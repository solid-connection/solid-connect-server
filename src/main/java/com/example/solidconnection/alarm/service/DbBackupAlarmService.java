package com.example.solidconnection.alarm.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/*
 * - DB EC2 는 private subnet 에 있어 Discord 로 직접 요청할 수 없다.
 * - 따라서 백업 실패 이벤트를 전달받아 Discord 로 중계한다.
 * */
@Service
@RequiredArgsConstructor
@Slf4j
public class DbBackupAlarmService {

    private static final String SUPPRESSION_KEY_PREFIX = "db-backup-alarm:";
    private static final Duration SUPPRESSION_TTL = Duration.ofMinutes(10);
    private static final String ROLE_MENTION_FORMAT = "<@&%s>";
    private static final String EMPTY_DETAIL = "-";

    private final DiscordWebhookSender discordWebhookSender;
    private final DbBackupAlarmProperties dbBackupAlarmProperties;
    private final InternalAlarmAuthProperties internalAlarmAuthProperties;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.profiles.active:}")
    private String environment;

    public void alarmBackupFailure(String token, DbBackupAlarmRequest request) {
        validateToken(token);

        String suppressionKey = buildSuppressionKey(request);
        if (isSuppressed(suppressionKey)) {
            return;
        }
        boolean isSent = discordWebhookSender.send(
                dbBackupAlarmProperties.webhookUrl(),
                buildMessage(request),
                mentionableRoleIds()
        );
        if (!isSent) {
            releaseSuppression(suppressionKey);
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

    private String buildSuppressionKey(DbBackupAlarmRequest request) {
        return SUPPRESSION_KEY_PREFIX + request.type().name() + ":" + request.instanceId();
    }

    /*
     * - 같은 유형과 인스턴스의 알림이 반복되면 일정 시간 동안 전송하지 않는다.
     * - Redis 를 사용할 수 없을 때는 알림 누락을 막기 위해 억제하지 않는다.
     * */
    private boolean isSuppressed(String suppressionKey) {
        try {
            Boolean isFirstAlarm = redisTemplate.opsForValue().setIfAbsent(suppressionKey, "1", SUPPRESSION_TTL);
            return !Boolean.TRUE.equals(isFirstAlarm);
        } catch (Exception e) {
            log.error("백업 알림 중복 억제 상태를 확인하지 못해 알림을 그대로 전송합니다. key={}", suppressionKey, e);
            return false;
        }
    }

    private String buildMessage(DbBackupAlarmRequest request) {
        return buildRoleMention() + "[%s] MySQL 백업 알림: %s\n인스턴스: %s\n발생 시각: %s\n상세: %s"
                .formatted(
                        environment.toUpperCase(),
                        request.type().getDisplayName(),
                        request.instanceId(),
                        request.occurredAt(),
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
     * - 전송에 실패하면 억제 상태를 되돌려 다음 백업 주기의 알림이 막히지 않게 한다.
     * */
    private void releaseSuppression(String suppressionKey) {
        try {
            redisTemplate.delete(suppressionKey);
        } catch (Exception e) {
            log.error("백업 알림 중복 억제 상태를 해제하지 못했습니다. key={}", suppressionKey, e);
        }
    }
}
