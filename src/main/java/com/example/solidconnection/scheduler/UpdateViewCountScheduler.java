package com.example.solidconnection.scheduler;

import com.example.solidconnection.community.post.service.ViewCountConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = "view.count.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class UpdateViewCountScheduler {

    private final ViewCountConsumer consumer;

    @Scheduled(fixedDelayString = "${view.count.scheduling.delay}")
    public void updateViewCount() {
        try {
            consumer.flushActiveCounters();
        } finally {
            // A failed flush must not prevent recovery of already persisted events.
            consumer.consume();
        }
    }
}
