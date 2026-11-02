package com.example.solidconnection.chat.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "chat.outbox")
@Getter
@Setter
public class ChatMessageOutboxProperties {

    @Positive
    private int maxAttempts = 5;
    @Positive
    private int batchSize = 100;
    @NotNull
    @DurationMin(nanos = 1)
    private Duration initialRetryDelay = Duration.ofSeconds(1);
    @NotNull
    @DurationMin(nanos = 1)
    private Duration maxRetryDelay = Duration.ofMinutes(1);
    @NotNull
    @DurationMin(nanos = 1)
    private Duration processingTimeout = Duration.ofMinutes(1);
}
