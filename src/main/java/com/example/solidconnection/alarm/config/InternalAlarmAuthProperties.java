package com.example.solidconnection.alarm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "internal-alarm")
public record InternalAlarmAuthProperties(
        String token
) {

}
