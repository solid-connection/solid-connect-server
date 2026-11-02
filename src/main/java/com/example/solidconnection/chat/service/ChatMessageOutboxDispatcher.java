package com.example.solidconnection.chat.service;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MICROS;

import com.example.solidconnection.chat.domain.ChatMessageOutboxCompletionType;
import com.example.solidconnection.chat.dto.ClaimedChatMessageOutbox;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageOutboxDispatcher {

    private static final int MAX_CONCURRENT_DISPATCHES = 4;

    private final ChatMessageOutboxTransactionService outboxTransactionService;
    private final SimpMessageSendingOperations simpMessageSendingOperations;
    private final Semaphore dispatchPermits = new Semaphore(MAX_CONCURRENT_DISPATCHES, true);

    public void dispatch(long outboxId) {
        try {
            dispatchPermits.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("채팅 메시지 Outbox 디스패치 대기 중 인터럽트되었습니다. outboxId={}", outboxId);
            return;
        }
        try {
            dispatchWithPermit(outboxId);
        } finally {
            dispatchPermits.release();
        }
    }

    private void dispatchWithPermit(long outboxId) {
        Optional<ClaimedChatMessageOutbox> claim;
        try {
            claim = outboxTransactionService.claim(outboxId, now());
        } catch (RuntimeException e) {
            log.error("채팅 메시지 Outbox 처리권을 획득하지 못했습니다. outboxId={}", outboxId, e);
            return;
        }
        claim.ifPresent(this::broadcast);
    }

    private void broadcast(ClaimedChatMessageOutbox claimed) {
        try {
            simpMessageSendingOperations.convertAndSend(
                    "/topic/chat/" + claimed.roomId(),
                    claimed.response()
            );
        } catch (RuntimeException e) {
            recordFailure(claimed, e);
            return;
        }

        try {
            boolean updated = outboxTransactionService.markCompleted(
                    claimed.outboxId(),
                    claimed.processingToken(),
                    ChatMessageOutboxCompletionType.BROKER_DISPATCHED,
                    now()
            );
            if (!updated) {
                log.info("이미 다른 경로에서 완료된 채팅 메시지 Outbox입니다. outboxId={}, messageId={}",
                        claimed.outboxId(), claimed.response().messageId());
            }
        } catch (RuntimeException e) {
            log.error("방송한 채팅 메시지 Outbox의 완료 상태를 기록하지 못했습니다. outboxId={}, messageId={}",
                    claimed.outboxId(), claimed.response().messageId(), e);
        }
    }

    private void recordFailure(ClaimedChatMessageOutbox claimed, RuntimeException broadcastException) {
        try {
            boolean updated = outboxTransactionService.recordFailure(
                    claimed.outboxId(),
                    claimed.processingToken(),
                    broadcastException.getMessage(),
                    now()
            );
            if (!updated) {
                log.info("이미 다른 경로에서 상태가 변경된 채팅 메시지 Outbox입니다. outboxId={}, messageId={}",
                        claimed.outboxId(), claimed.response().messageId());
            }
        } catch (RuntimeException stateException) {
            broadcastException.addSuppressed(stateException);
        }
        log.error("채팅 메시지를 방송하지 못했습니다. outboxId={}, roomId={}, messageId={}",
                claimed.outboxId(), claimed.roomId(), claimed.response().messageId(), broadcastException);
    }

    private ZonedDateTime now() {
        return ZonedDateTime.now(UTC).truncatedTo(MICROS);
    }
}
