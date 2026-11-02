package com.example.solidconnection.chat.service;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MICROS;

import com.example.solidconnection.chat.config.ChatMessageOutboxProperties;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "chat.outbox.scheduling.enabled", havingValue = "true")
public class ChatMessageOutboxScheduler {

    private final ChatMessageOutboxTransactionService outboxTransactionService;
    private final ChatMessageOutboxDispatcher dispatcher;
    private final ChatMessageOutboxProperties properties;

    @Scheduled(fixedDelayString = "${chat.outbox.scheduling.delay:5000}")
    public void dispatchPendingMessages() {
        ZonedDateTime now = ZonedDateTime.now(UTC).truncatedTo(MICROS);
        outboxTransactionService.recoverStaleProcessing(now.minus(properties.getProcessingTimeout()), now);
        outboxTransactionService.findDispatchableIds(now).forEach(dispatcher::dispatch);
    }
}
