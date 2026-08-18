package com.example.solidconnection.alarm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discord.db-backup-fail-alarm")
public record DbBackupAlarmProperties(
        String webhookUrl,
        String mentionRoleId
) {

}
