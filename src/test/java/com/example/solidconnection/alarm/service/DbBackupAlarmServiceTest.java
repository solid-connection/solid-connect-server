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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final Instant OCCURRED_AT = Instant.parse("2026-08-17T03:00:00Z");

    @Autowired
    private DbBackupAlarmService dbBackupAlarmService;

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
                    () -> assertThat(contentCaptor.getValue()).contains("2026-08-17T03:00:00Z"),
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

        @Test
        void 멘션_문자열_뒤에_알림_본문이_이어진다() {
            // given
            DbBackupAlarmRequest request = 백업_알림_요청(DbBackupAlarmType.BINLOG_GAP_DETECTED, "i-mention-body-target");

            // when
            dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request);

            // then
            ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
            verify(discordWebhookSender).send(anyString(), contentCaptor.capture(), anyList());
            assertThat(contentCaptor.getValue())
                    .contains("MySQL 백업 알림: " + DbBackupAlarmType.BINLOG_GAP_DETECTED.getDisplayName());
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
    @DisplayName("중복 알림 억제")
    class 중복_알림을_억제한다 {

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
        void 인스턴스가_다르면_각각_전송한다() {
            // given
            String firstInstanceId = "i-first-target";
            String secondInstanceId = "i-second-target";

            // when
            dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, 백업_알림_요청(DbBackupAlarmType.DUMP_FAILED, firstInstanceId));
            dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, 백업_알림_요청(DbBackupAlarmType.DUMP_FAILED, secondInstanceId));

            // then
            assertAll(
                    () -> verify(discordWebhookSender, times(1)).send(anyString(), contains(firstInstanceId), anyList()),
                    () -> verify(discordWebhookSender, times(1)).send(anyString(), contains(secondInstanceId), anyList())
            );
        }

        @Test
        void 전송에_실패하면_억제를_해제해_다음_요청을_다시_전송한다() {
            // given
            String instanceId = "i-send-failed-target";
            reset(discordWebhookSender);
            when(discordWebhookSender.send(anyString(), anyString(), anyList())).thenReturn(false);
            DbBackupAlarmRequest request = 백업_알림_요청(DbBackupAlarmType.DUMP_FAILED, instanceId);

            // when
            dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request);
            dbBackupAlarmService.alarmBackupFailure(VALID_TOKEN, request);

            // then
            verify(discordWebhookSender, times(2)).send(anyString(), contains(instanceId), anyList());
        }
    }
}
