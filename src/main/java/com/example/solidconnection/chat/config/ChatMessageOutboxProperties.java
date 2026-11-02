package com.example.solidconnection.chat.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "chat.outbox")
@Getter
@Setter
public class ChatMessageOutboxProperties {

    private int maxAttempts = 5;
    private int batchSize = 100;
    private Duration initialRetryDelay = Duration.ofSeconds(1);
    private Duration maxRetryDelay = Duration.ofMinutes(1);
    private Duration processingTimeout = Duration.ofMinutes(1);
}
