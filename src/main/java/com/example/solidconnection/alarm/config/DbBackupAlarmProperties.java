package com.example.solidconnection.alarm.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/*
 * - webhookUrl 이 없으면 알림을 보낼 수 없으므로 기동 시점에 검증한다.
 * - mentionRoleId 는 선택 값이며, 없으면 멘션 없이 알림만 보낸다.
 * */
@Validated
@ConfigurationProperties(prefix = "discord.db-backup-fail-alarm")
public record DbBackupAlarmProperties(

        @NotBlank
        String webhookUrl,

        String mentionRoleId
) {

}
