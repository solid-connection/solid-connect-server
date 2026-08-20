package com.example.solidconnection.alarm.service;

import static com.example.solidconnection.common.exception.ErrorCode.DB_BACKUP_ALARM_SEND_FAILED;
import static com.example.solidconnection.common.exception.ErrorCode.INTERNAL_ALARM_UNAUTHORIZED;

import com.example.solidconnection.alarm.config.DbBackupAlarmProperties;
import com.example.solidconnection.alarm.config.InternalAlarmAuthProperties;
import com.example.solidconnection.alarm.dto.DbBackupAlarmRequest;
import com.example.solidconnection.common.discord.DiscordWebhookSender;
import com.example.solidconnection.common.exception.CustomException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

/*
 * - DB EC2 는 private subnet 에 있어 Discord 로 직접 요청할 수 없다. 따라서 백업 실패 이벤트를 전달받아 Discord 로 중계한다.
 * - 같은 실패가 반복될 때 알림이 쌓이지 않도록 억제 간격을 점점 늘린다.
 * - 억제 상태는 Redis 에 두고 원자적 연산으로 갱신하므로, 서버가 여러 대여도 한 대만 알림을 보낸다.
 * */
@Service
@Slf4j
public class DbBackupAlarmService {

    private static final String MUTE_KEY_PREFIX = "db-backup-alarm:mute:";
    private static final String COUNT_KEY_PREFIX = "db-backup-alarm:count:";
    private static final Duration COUNT_TTL = Duration.ofHours(12);
    private static final List<Duration> MUTE_DURATIONS = List.of(
            Duration.ofMinutes(5),
            Duration.ofMinutes(15),
            Duration.ofHours(1),
            Duration.ofHours(6)
    );
    private static final String ROLE_MENTION_FORMAT = "<@&%s>";
    private static final String EMPTY_DETAIL = "-";

    private final DiscordWebhookSender discordWebhookSender;
    private final DbBackupAlarmProperties dbBackupAlarmProperties;
    private final InternalAlarmAuthProperties internalAlarmAuthProperties;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<Long> releaseDbBackupAlarmLuaScript;

    @Value("${spring.profiles.active:}")
    private String environment;

    public DbBackupAlarmService(
            DiscordWebhookSender discordWebhookSender,
            DbBackupAlarmProperties dbBackupAlarmProperties,
            InternalAlarmAuthProperties internalAlarmAuthProperties,
            RedisTemplate<String, String> redisTemplate,
            @Qualifier("releaseDbBackupAlarmScript") RedisScript<Long> releaseDbBackupAlarmLuaScript
    ) {
        this.discordWebhookSender = discordWebhookSender;
        this.dbBackupAlarmProperties = dbBackupAlarmProperties;
        this.internalAlarmAuthProperties = internalAlarmAuthProperties;
        this.redisTemplate = redisTemplate;
        this.releaseDbBackupAlarmLuaScript = releaseDbBackupAlarmLuaScript;
    }

    public void alarmBackupFailure(String token, DbBackupAlarmRequest request) {
        validateToken(token);

        String alarmKey = buildAlarmKey(request);
        String muteKey = MUTE_KEY_PREFIX + alarmKey;
        String countKey = COUNT_KEY_PREFIX + alarmKey;

        if (!acquireAlarmGate(muteKey)) {
            return;
        }
        long alarmCount = increaseAlarmCount(countKey);
        Duration muteDuration = resolveMuteDuration(alarmCount);
        extendAlarmGate(muteKey, muteDuration);

        boolean isSent = discordWebhookSender.send(
                dbBackupAlarmProperties.webhookUrl(),
                buildMessage(request, alarmCount, muteDuration),
                mentionableRoleIds()
        );
        if (!isSent) {
            releaseAlarmGate(muteKey, countKey);
            throw new CustomException(DB_BACKUP_ALARM_SEND_FAILED);
        }
    }

    /*
     * - 토큰이 설정되지 않은 환경에서는 모든 요청을 거부한다.
     * - 설정 누락과 토큰 불일치를 같은 응답으로 처리해 내부 상태가 드러나지 않게 한다.
     * */
    private void validateToken(String token) {
        String configuredToken = internalAlarmAuthProperties.token();
        if (configuredToken == null || configuredToken.isBlank()) {
            log.error("내부 알림 인증 토큰이 설정되지 않아 요청을 거부했습니다.");
            throw new CustomException(INTERNAL_ALARM_UNAUTHORIZED);
        }
        if (token == null || !MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8),
                configuredToken.getBytes(StandardCharsets.UTF_8))) {
            throw new CustomException(INTERNAL_ALARM_UNAUTHORIZED);
        }
    }

    private String buildAlarmKey(DbBackupAlarmRequest request) {
        return request.type().name() + ":" + request.instanceId();
    }

    /*
     * - setIfAbsent 는 원자적이므로 여러 서버가 동시에 요청받아도 한 대만 통과한다.
     * - 통과하지 못하면 억제 중이거나 다른 서버가 방금 알림을 보낸 것이므로 전송하지 않는다.
     * - Redis 를 사용할 수 없을 때는 알림 누락을 막기 위해 통과시킨다.
     * */
    private boolean acquireAlarmGate(String muteKey) {
        try {
            Boolean isAcquired = redisTemplate.opsForValue()
                    .setIfAbsent(muteKey, "1", MUTE_DURATIONS.getFirst());
            return Boolean.TRUE.equals(isAcquired);
        } catch (Exception e) {
            log.error("백업 알림 억제 상태를 확인하지 못해 알림을 그대로 전송합니다. key={}", muteKey, e);
            return true;
        }
    }

    /*
     * - 게이트를 통과한 요청만 카운트하므로 서버가 여러 대여도 연속 발생 횟수가 부풀지 않는다.
     * */
    private long increaseAlarmCount(String countKey) {
        try {
            Long alarmCount = redisTemplate.opsForValue().increment(countKey);
            redisTemplate.expire(countKey, COUNT_TTL);
            if (alarmCount == null) {
                return 1L;
            }
            return alarmCount;
        } catch (Exception e) {
            log.error("백업 알림 연속 발생 횟수를 증가하지 못했습니다. key={}", countKey, e);
            return 1L;
        }
    }

    private Duration resolveMuteDuration(long alarmCount) {
        int index = (int) Math.min(alarmCount, MUTE_DURATIONS.size()) - 1;
        return MUTE_DURATIONS.get(Math.max(index, 0));
    }

    /*
     * - 최초 잠금은 가장 짧은 간격으로 걸어두고, 연속 발생 횟수에 맞는 간격으로 늘린다.
     * */
    private void extendAlarmGate(String muteKey, Duration muteDuration) {
        try {
            redisTemplate.expire(muteKey, muteDuration);
        } catch (Exception e) {
            log.error("백업 알림 억제 간격을 늘리지 못했습니다. key={}", muteKey, e);
        }
    }

    private String buildMessage(DbBackupAlarmRequest request, long alarmCount, Duration muteDuration) {
        return buildRoleMention()
                + "[%s] [%s] MySQL 백업 알림: %s\n인스턴스: %s\n발생 시각: %s\n연속 발생: %d회 (다음 %s 동안 같은 알림을 보내지 않습니다)\n상세: %s"
                .formatted(
                        environment.toUpperCase(),
                        request.type().getSeverity().getDisplayName(),
                        request.type().getDisplayName(),
                        request.instanceId(),
                        request.occurredAt(),
                        alarmCount,
                        formatDuration(muteDuration),
                        resolveDetail(request.detail())
                );
    }

    /*
     * - 멘션할 역할이 설정되지 않으면 멘션 없이 알림만 보낸다.
     * */
    private String buildRoleMention() {
        String mentionRoleId = dbBackupAlarmProperties.mentionRoleId();
        if (mentionRoleId == null || mentionRoleId.isBlank()) {
            return "";
        }
        return ROLE_MENTION_FORMAT.formatted(mentionRoleId) + "\n";
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        if (hours > 0) {
            return hours + "시간";
        }
        return duration.toMinutes() + "분";
    }

    private String resolveDetail(String detail) {
        if (detail == null || detail.isBlank()) {
            return EMPTY_DETAIL;
        }
        return detail;
    }

    private List<String> mentionableRoleIds() {
        String mentionRoleId = dbBackupAlarmProperties.mentionRoleId();
        if (mentionRoleId == null || mentionRoleId.isBlank()) {
            return List.of();
        }
        return List.of(mentionRoleId);
    }

    /*
     * - 전송에 실패하면 억제와 횟수를 되돌려 다음 요청이 다시 알림을 시도할 수 있게 한다.
     * - dump 는 하루 한 번 실행되므로 실패를 그대로 두면 그날의 알림이 사라진다.
     * - 억제 해제와 횟수 감소를 나누어 실행하면 그 사이에 다른 서버가 증가시킨 횟수를 잘못 줄이므로 lua 로 함께 처리한다.
     * */
    private void releaseAlarmGate(String muteKey, String countKey) {
        try {
            redisTemplate.execute(releaseDbBackupAlarmLuaScript, List.of(muteKey, countKey));
        } catch (Exception e) {
            log.error("백업 알림 억제 상태를 해제하지 못했습니다. key={}", muteKey, e);
        }
    }
}
