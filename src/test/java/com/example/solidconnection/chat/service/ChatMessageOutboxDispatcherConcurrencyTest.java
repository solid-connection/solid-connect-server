package com.example.solidconnection.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.solidconnection.chat.dto.ChatMessageSendResponse;
import com.example.solidconnection.chat.dto.ClaimedChatMessageOutbox;
import com.example.solidconnection.chat.domain.MessageType;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

class ChatMessageOutboxDispatcherConcurrencyTest {

    @Test
    void 동시_디스패치를_네_개로_제한하면서_대기_작업을_버리지_않는다() throws Exception {
        // given
        ChatMessageOutboxTransactionService transactionService =
                BDDMockito.mock(ChatMessageOutboxTransactionService.class);
        SimpMessageSendingOperations messagingOperations =
                BDDMockito.mock(SimpMessageSendingOperations.class);
        ChatMessageOutboxDispatcher dispatcher =
                new ChatMessageOutboxDispatcher(transactionService, messagingOperations);
        AtomicInteger claimedCount = new AtomicInteger();
        CountDownLatch fourBroadcastsStarted = new CountDownLatch(4);
        CountDownLatch releaseBroadcasts = new CountDownLatch(1);

        BDDMockito.given(transactionService.claim(BDDMockito.anyLong(), BDDMockito.any(ZonedDateTime.class)))
                .willAnswer(invocation -> {
                    long outboxId = invocation.getArgument(0);
                    claimedCount.incrementAndGet();
                    return Optional.of(claimed(outboxId));
                });
        BDDMockito.willAnswer(invocation -> {
            fourBroadcastsStarted.countDown();
            releaseBroadcasts.await(3, TimeUnit.SECONDS);
            return null;
        }).given(messagingOperations).convertAndSend(
                BDDMockito.anyString(),
                BDDMockito.any(ChatMessageSendResponse.class)
        );
        BDDMockito.given(transactionService.markCompleted(
                BDDMockito.anyLong(),
                BDDMockito.anyString(),
                BDDMockito.any(),
                BDDMockito.any(ZonedDateTime.class)
        )).willReturn(true);

        try (var executor = Executors.newFixedThreadPool(5)) {
            // when
            List<? extends java.util.concurrent.Future<?>> futures = java.util.stream.LongStream.rangeClosed(1, 5)
                    .mapToObj(outboxId -> executor.submit(() -> dispatcher.dispatch(outboxId)))
                    .toList();

            // then
            assertThat(fourBroadcastsStarted.await(1, TimeUnit.SECONDS)).isTrue();
            await().during(Duration.ofMillis(100))
                    .atMost(Duration.ofSeconds(1))
                    .untilAsserted(() -> assertThat(claimedCount).hasValue(4));

            releaseBroadcasts.countDown();
            for (var future : futures) {
                future.get(1, TimeUnit.SECONDS);
            }
            assertThat(claimedCount).hasValue(5);
        } finally {
            releaseBroadcasts.countDown();
        }
    }

    private ClaimedChatMessageOutbox claimed(long outboxId) {
        return new ClaimedChatMessageOutbox(
                outboxId,
                "token-" + outboxId,
                1,
                1,
                new ChatMessageSendResponse(outboxId, "content", 1, MessageType.TEXT, List.of())
        );
    }
}
