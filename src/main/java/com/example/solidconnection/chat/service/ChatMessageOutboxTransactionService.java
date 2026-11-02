package com.example.solidconnection.chat.service;

import com.example.solidconnection.chat.config.ChatMessageOutboxProperties;
import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.domain.ChatMessageOutbox;
import com.example.solidconnection.chat.domain.ChatMessageOutboxCompletionType;
import com.example.solidconnection.chat.domain.ChatMessageOutboxStatus;
import com.example.solidconnection.chat.domain.ChatParticipant;
import com.example.solidconnection.chat.dto.ChatMessageSendResponse;
import com.example.solidconnection.chat.dto.ClaimedChatMessageOutbox;
import com.example.solidconnection.chat.repository.ChatMessageOutboxRepository;
import com.example.solidconnection.chat.repository.ChatParticipantRepository;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatMessageOutboxTransactionService {

    private static final int LAST_ERROR_MAX_LENGTH = 1000;

    private final ChatMessageOutboxRepository outboxRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageOutboxProperties properties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<ClaimedChatMessageOutbox> claim(long outboxId, ZonedDateTime now) {
        String processingToken = UUID.randomUUID().toString();
        int updatedCount = outboxRepository.claim(
                outboxId,
                processingToken,
                now,
                properties.getMaxAttempts(),
                ChatMessageOutboxStatus.PENDING,
                ChatMessageOutboxStatus.PROCESSING
        );
        if (updatedCount == 0) {
            return Optional.empty();
        }

        Optional<ChatMessageOutbox> foundOutbox = outboxRepository.findWithMessageById(outboxId);
        if (foundOutbox.isEmpty()) {
            outboxRepository.recordFailure(
                    outboxId,
                    processingToken,
                    ChatMessageOutboxStatus.PROCESSING,
                    ChatMessageOutboxStatus.FAILED,
                    null,
                    "message unavailable",
                    now
            );
            return Optional.empty();
        }
        ChatMessageOutbox outbox = foundOutbox.get();
        ChatMessage chatMessage = outbox.getChatMessage();
        ChatParticipant sender = chatParticipantRepository.findById(chatMessage.getSenderId()).orElseThrow();
        ChatMessageSendResponse response = ChatMessageSendResponse.of(chatMessage, sender.getSiteUserId());

        return Optional.of(new ClaimedChatMessageOutbox(
                outbox.getId(),
                processingToken,
                outbox.getAttemptCount(),
                chatMessage.getChatRoom().getId(),
                response
        ));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markCompleted(
            long outboxId,
            String processingToken,
            ChatMessageOutboxCompletionType completionType,
            ZonedDateTime completedAt
    ) {
        return outboxRepository.markCompleted(
                outboxId,
                processingToken,
                ChatMessageOutboxStatus.PROCESSING,
                ChatMessageOutboxStatus.COMPLETED,
                completionType,
                completedAt
        ) == 1;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean recordFailure(
            long outboxId,
            String processingToken,
            String lastError,
            ZonedDateTime failedAt
    ) {
        ChatMessageOutbox outbox = outboxRepository.findById(outboxId).orElseThrow();
        boolean exhausted = outbox.getAttemptCount() >= properties.getMaxAttempts();
        ChatMessageOutboxStatus nextStatus = exhausted
                ? ChatMessageOutboxStatus.FAILED
                : ChatMessageOutboxStatus.PENDING;
        ZonedDateTime nextAttemptAt = exhausted
                ? null
                : failedAt.plus(calculateBackoff(outbox.getAttemptCount()));

        return outboxRepository.recordFailure(
                outboxId,
                processingToken,
                ChatMessageOutboxStatus.PROCESSING,
                nextStatus,
                nextAttemptAt,
                truncate(lastError),
                failedAt
        ) == 1;
    }

    private Duration calculateBackoff(int attemptCount) {
        Duration delay = properties.getInitialRetryDelay();
        for (int attempt = 1; attempt < attemptCount; attempt++) {
            if (delay.compareTo(properties.getMaxRetryDelay()) >= 0) {
                return properties.getMaxRetryDelay();
            }
            delay = delay.multipliedBy(2);
        }
        return delay.compareTo(properties.getMaxRetryDelay()) > 0
                ? properties.getMaxRetryDelay()
                : delay;
    }

    private String truncate(String value) {
        if (value == null || value.length() <= LAST_ERROR_MAX_LENGTH) {
            return value;
        }
        return value.substring(0, LAST_ERROR_MAX_LENGTH);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recoverStaleProcessing(ZonedDateTime cutoff, ZonedDateTime now) {
        int recoveredCount = outboxRepository.recoverRetryableStaleProcessing(
                ChatMessageOutboxStatus.PROCESSING,
                ChatMessageOutboxStatus.PENDING,
                cutoff,
                now,
                properties.getMaxAttempts()
        );
        int failedCount = outboxRepository.failExhaustedStaleProcessing(
                ChatMessageOutboxStatus.PROCESSING,
                ChatMessageOutboxStatus.FAILED,
                cutoff,
                now,
                properties.getMaxAttempts()
        );
        return recoveredCount + failedCount;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<Long> findDispatchableIds(ZonedDateTime now) {
        return outboxRepository.findDispatchableIds(
                ChatMessageOutboxStatus.PENDING,
                now,
                properties.getMaxAttempts(),
                PageRequest.of(0, properties.getBatchSize())
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int markHistoryServed(
            long recipientParticipantId,
            List<Long> messageIds,
            ZonedDateTime completedAt
    ) {
        if (messageIds.isEmpty()) {
            return 0;
        }
        return outboxRepository.markHistoryServed(
                recipientParticipantId,
                messageIds,
                List.of(ChatMessageOutboxStatus.FAILED),
                ChatMessageOutboxStatus.COMPLETED,
                ChatMessageOutboxCompletionType.HISTORY_SERVED,
                completedAt
        );
    }
}
