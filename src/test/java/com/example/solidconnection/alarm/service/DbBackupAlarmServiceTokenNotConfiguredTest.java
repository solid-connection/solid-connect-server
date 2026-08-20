package com.example.solidconnection.alarm.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.solidconnection.alarm.domain.DbBackupAlarmType;
import com.example.solidconnection.alarm.dto.DbBackupAlarmRequest;
import com.example.solidconnection.common.discord.DiscordWebhookSender;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestContainerSpringBootTest
@TestPropertySource(properties = {
        "internal-alarm.token=",
        "discord.db-backup-fail-alarm.webhook-url=https://discord.test/webhooks/db-backup"
})
@DisplayName("DB 백업 알림 인증 토큰 미설정 테스트")
class DbBackupAlarmServiceTokenNotConfiguredTest {

    @Autowired
    private DbBackupAlarmService dbBackupAlarmService;

    @MockitoBean
    private DiscordWebhookSender discordWebhookSender;

    @Test
    void 인증_토큰이_설정되지_않으면_어떤_토큰으로_요청해도_예외_응답을_반환한다() {
        // given
        DbBackupAlarmRequest request = new DbBackupAlarmRequest(
                DbBackupAlarmType.DUMP_FAILED, "i-0c5d57927ef9ca426", Instant.parse("2026-08-17T03:00:00Z"), null);

        // when, then
        assertAll(
                () -> assertThatThrownBy(() -> dbBackupAlarmService.alarmBackupFailure("any-token", request))
                        .isInstanceOf(CustomException.class),
                () -> assertThatThrownBy(() -> dbBackupAlarmService.alarmBackupFailure("", request))
                        .isInstanceOf(CustomException.class),
                () -> assertThatThrownBy(() -> dbBackupAlarmService.alarmBackupFailure(null, request))
                        .isInstanceOf(CustomException.class)
        );
        verify(discordWebhookSender, never()).send(anyString(), anyString(), anyList());
    }
}
