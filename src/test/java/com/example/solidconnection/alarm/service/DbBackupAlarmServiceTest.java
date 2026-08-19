package com.example.solidconnection.alarm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.solidconnection.alarm.domain.DbBackupAlarmType;
import com.example.solidconnection.alarm.dto.DbBackupAlarmRequest;
import com.example.solidconnection.common.discord.DiscordWebhookSender;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestContainerSpringBootTest
@TestPropertySource(properties = {
        "internal-alarm.token=test-internal-alarm-token",
        "discord.db-backup-fail-alarm.webhook-url=https://discord.test/webhooks/db-backup",
        "discord.db-backup-fail-alarm.mention-role-id=1234567890"
})
@DisplayName("DB 백업 알림 서비스 테스트")
class DbBackupAlarmServiceTest {

    private static final String VALID_TOKEN = "test-internal-alarm-token";
    private static final String WEBHOOK_URL = "https://discord.test/webhooks/db-backup";
    private static final String MENTION_ROLE_ID = "1234567890";
    private static final String INSTANCE_ID = "i-0c5d57927ef9ca426";
    private static final Instant OCCURRED_AT = Instant.parse("2026-08-18T03:00:00Z");
    private static final String MUTE_KEY_PREFIX = "db-backup-alarm:mute:";

    @Autowired
    private DbBackupAlarmService dbBackupAlarmService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    private DiscordWebhookSender discordWebhookSender;

    @BeforeEach
    void setUp() {
        reset(discordWebhookSender);
        when(discordWebhookSender.send(anyString(), anyString(), anyList())).thenReturn(true);
    }

    private DbBackupAlarmRequest 백업_알림_요청(DbBackupAlarmType type, String instanceId) {
        return new DbBackupAlarmRequest(type, instanceId, OCCURRED_AT, "exit code 1");
    }

    private String 억제_키(DbBackupAlarmType type, String instanceId) {
        return MUTE_KEY_PREFIX + type.name() + ":" + instanceId;
    }

    @Nested
    @DisplayName("백업 실패 알림 전송")
    class 백업_실패_알림을_전송한다 {

        @Test
        void 유효한_토큰으로_요청하면_설정된_webhook_으로_알림을_전송한다() {
            // given
            DbBackupAlarmRequest request = 백업_알림_요청(DbBackupAlarmType.DUMP_FAILED, INSTANCE_ID);

            // when
            dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request);

            // then
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
            verify(discordWebhookSender).send(urlCaptor.capture(), contentCaptor.capture(), anyList());
            assertAll(
                    () -> assertThat(urlCaptor.getValue()).isEqualTo(WEBHOOK_URL),
                    () -> assertThat(contentCaptor.getValue()).contains(DbBackupAlarmType.DUMP_FAILED.getDisplayName()),
                    () -> assertThat(contentCaptor.getValue()).contains(INSTANCE_ID),
                    () -> assertThat(contentCaptor.getValue()).contains("2026-08-18T03:00:00Z"),
                    () -> assertThat(contentCaptor.getValue()).contains("exit code 1")
            );
        }

        @Test
        void 상세_내용이_없으면_비어_있음을_표시해_전송한다() {
            // given
            DbBackupAlarmRequest request = new DbBackupAlarmRequest(
                    DbBackupAlarmType.BINLOG_UPLOAD_DELAYED, "i-detail-absent", OCCURRED_AT, null);

            // when
            dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request);

            // then
            verify(discordWebhookSender).send(anyString(), contains("상세: -"), anyList());
        }
    }

    @Nested
    @DisplayName("알림 심각도")
    class 알림_심각도를_표시한다 {

        @Test
        void 덤프_실패는_심각으로_표시한다() {
            // when
            dbBackupAlarmService.alarmBackupFailure(
                    VALID_TOKEN, 백업_알림_요청(DbBackupAlarmType.DUMP_FAILED, "i-severity-critical"));

            // then
            verify(discordWebhookSender).send(anyString(), contains("[심각]"), anyList());
        }

        @Test
        void 업로드_지연은_경고로_표시한다() {
            // when
            dbBackupAlarmService.alarmBackupFailure(
                    VALID_TOKEN, 백업_알림_요청(DbBackupAlarmType.BINLOG_UPLOAD_DELAYED, "i-severity-warning"));

            // then
            verify(discordWebhookSender).send(anyString(), contains("[경고]"), anyList());
        }
    }

    @Nested
    @DisplayName("담당 역할 멘션")
    class 담당_역할을_멘션한다 {

        @Test
        void 메시지_앞에_역할_멘션을_붙이고_해당_역할만_멘션을_허용한다() {
            // given
            DbBackupAlarmRequest request = 백업_알림_요청(DbBackupAlarmType.DUMP_FAILED, "i-mention-target");

            // when
            dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request);

            // then
            ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<List<String>> roleIdsCaptor = ArgumentCaptor.forClass(List.class);
            verify(discordWebhookSender).send(anyString(), contentCaptor.capture(), roleIdsCaptor.capture());
            assertAll(
                    () -> assertThat(contentCaptor.getValue()).startsWith("<@&%s>".formatted(MENTION_ROLE_ID)),
                    () -> assertThat(roleIdsCaptor.getValue()).containsExactly(MENTION_ROLE_ID)
            );
        }
    }

    @Nested
    @DisplayName("내부 호출자 인증")
    class 내부_호출자를_인증한다 {

        @Test
        void 토큰이_일치하지_않으면_예외_응답을_반환한다() {
            // given
            DbBackupAlarmRequest request = 백업_알림_요청(DbBackupAlarmType.DUMP_FAILED, INSTANCE_ID);

            // when, then
            assertThatThrownBy(() -> dbBackupAlarmService.alarmBackupFailure("wrong-token", request))
                    .isInstanceOf(CustomException.class);
            verify(discordWebhookSender, never()).send(anyString(), anyString(), anyList());
        }

        @Test
        void 토큰이_없으면_예외_응답을_반환한다() {
            // given
            DbBackupAlarmRequest request = 백업_알림_요청(DbBackupAlarmType.DUMP_FAILED, INSTANCE_ID);

            // when, then
            assertThatThrownBy(() -> dbBackupAlarmService.alarmBackupFailure(null, request))
                    .isInstanceOf(CustomException.class);
            verify(discordWebhookSender, never()).send(anyString(), anyString(), anyList());
        }
    }

    @Nested
    @DisplayName("알림 피로도 억제")
    class 알림_피로도를_억제한다 {

        @Test
        void 같은_유형과_인스턴스로_반복_요청하면_한_번만_전송한다() {
            // given
            String instanceId = "i-duplicated-target";
            DbBackupAlarmRequest request = 백업_알림_요청(DbBackupAlarmType.DUMP_FAILED, instanceId);

            // when
            dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request);
            dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request);

            // then
            verify(discordWebhookSender, times(1)).send(anyString(), contains(instanceId), anyList());
        }

        @Test
        void 유형이_다르면_각각_전송한다() {
            // given
            String instanceId = "i-different-type-target";

            // when
            dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, 백업_알림_요청(DbBackupAlarmType.DUMP_FAILED, instanceId));
            dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, 백업_알림_요청(DbBackupAlarmType.BINLOG_UPLOAD_FAILED, instanceId));

            // then
            assertAll(
                    () -> verify(discordWebhookSender)
                            .send(anyString(), contains(DbBackupAlarmType.DUMP_FAILED.getDisplayName()), anyList()),
                    () -> verify(discordWebhookSender)
                            .send(anyString(), contains(DbBackupAlarmType.BINLOG_UPLOAD_FAILED.getDisplayName()), anyList())
            );
        }

        @Test
        void 연속_발생_횟수가_늘어나면_억제_간격도_늘어난다() {
            // given
            String instanceId = "i-backoff-target";
            DbBackupAlarmRequest request = 백업_알림_요청(DbBackupAlarmType.DUMP_FAILED, instanceId);
            String muteKey = 억제_키(DbBackupAlarmType.DUMP_FAILED, instanceId);

            // when - 억제를 강제로 풀어 다음 단계를 관찰한다
            for (int i = 0; i < 5; i++) {
                dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request);
                redisTemplate.delete(muteKey);
            }

            // then
            ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
            verify(discordWebhookSender, times(5)).send(anyString(), contentCaptor.capture(), anyList());
            List<String> contents = contentCaptor.getAllValues();
            assertAll(
                    () -> assertThat(contents.get(0)).contains("연속 발생: 1회").contains("다음 5분"),
                    () -> assertThat(contents.get(1)).contains("연속 발생: 2회").contains("다음 15분"),
                    () -> assertThat(contents.get(2)).contains("연속 발생: 3회").contains("다음 1시간"),
                    () -> assertThat(contents.get(3)).contains("연속 발생: 4회").contains("다음 6시간"),
                    () -> assertThat(contents.get(4)).contains("연속 발생: 5회").contains("다음 6시간")
            );
        }

        @Test
        void 억제_간격은_연속_발생_횟수에_맞게_남은_시간으로_설정된다() {
            // given
            String instanceId = "i-mute-ttl-target";
            DbBackupAlarmRequest request = 백업_알림_요청(DbBackupAlarmType.DUMP_FAILED, instanceId);
            String muteKey = 억제_키(DbBackupAlarmType.DUMP_FAILED, instanceId);

            // when
            dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request);
            Long firstTtl = redisTemplate.getExpire(muteKey, TimeUnit.SECONDS);
            redisTemplate.delete(muteKey);
            dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request);
            Long secondTtl = redisTemplate.getExpire(muteKey, TimeUnit.SECONDS);

            // then
            assertAll(
                    () -> assertThat(firstTtl).isBetween(1L, 300L),
                    () -> assertThat(secondTtl).isBetween(301L, 900L)
            );
        }
    }

    @Nested
    @DisplayName("다중 서버 동시 요청")
    class 다중_서버에서_동시에_요청해도_안전하다 {

        @Test
        void 동시에_같은_알림이_들어오면_한_번만_전송한다() throws Exception {
            // given
            int threadCount = 10;
            String instanceId = "i-concurrent-target";
            DbBackupAlarmRequest request = 백업_알림_요청(DbBackupAlarmType.DUMP_FAILED, instanceId);
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);

            // when
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        startLatch.await();
                        dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request);
                    } catch (Exception e) {
                        // 억제된 요청과 인터럽트는 무시한다
                    } finally {
                        endLatch.countDown();
                    }
                });
            }
            startLatch.countDown();
            endLatch.await(10, TimeUnit.SECONDS);
            executorService.shutdown();

            // then
            verify(discordWebhookSender, times(1)).send(anyString(), contains(instanceId), anyList());
        }
    }

    @Nested
    @DisplayName("전송 실패 처리")
    class 전송_실패를_처리한다 {

        @Test
        void 전송에_실패하면_502_응답을_반환한다() {
            // given
            reset(discordWebhookSender);
            when(discordWebhookSender.send(anyString(), anyString(), anyList())).thenReturn(false);
            DbBackupAlarmRequest request = 백업_알림_요청(DbBackupAlarmType.DUMP_FAILED, "i-send-failed-status");

            // when, then
            assertThatThrownBy(() -> dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("code")
                    .isEqualTo(HttpStatus.BAD_GATEWAY.value());
        }

        @Test
        void 전송에_실패하면_억제를_해제해_다음_요청을_다시_전송한다() {
            // given
            String instanceId = "i-send-failed-target";
            reset(discordWebhookSender);
            when(discordWebhookSender.send(anyString(), anyString(), anyList())).thenReturn(false);
            DbBackupAlarmRequest request = 백업_알림_요청(DbBackupAlarmType.DUMP_FAILED, instanceId);

            // when
            assertThatThrownBy(() -> dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request))
                    .isInstanceOf(CustomException.class);
            assertThatThrownBy(() -> dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request))
                    .isInstanceOf(CustomException.class);

            // then
            verify(discordWebhookSender, times(2)).send(anyString(), contains(instanceId), anyList());
        }

        @Test
        void 전송에_실패하면_연속_발생_횟수가_증가하지_않는다() {
            // given
            String instanceId = "i-count-rollback-target";
            String muteKey = 억제_키(DbBackupAlarmType.DUMP_FAILED, instanceId);
            DbBackupAlarmRequest request = 백업_알림_요청(DbBackupAlarmType.DUMP_FAILED, instanceId);
            reset(discordWebhookSender);
            when(discordWebhookSender.send(anyString(), anyString(), anyList())).thenReturn(false);
            assertThatThrownBy(() -> dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request))
                    .isInstanceOf(CustomException.class);

            // when - 이번에는 전송이 성공한다
            reset(discordWebhookSender);
            when(discordWebhookSender.send(anyString(), anyString(), anyList())).thenReturn(true);
            redisTemplate.delete(muteKey);
            dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request);

            // then - 실패한 시도가 횟수에 반영되지 않아 다시 1회차로 알린다
            verify(discordWebhookSender).send(anyString(), contains("연속 발생: 1회"), anyList());
        }
    }
}
