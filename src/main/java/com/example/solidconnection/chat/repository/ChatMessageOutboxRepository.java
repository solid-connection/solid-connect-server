package com.example.solidconnection.chat.repository;

import com.example.solidconnection.chat.domain.ChatMessageOutbox;
import com.example.solidconnection.chat.domain.ChatMessageOutboxCompletionType;
import com.example.solidconnection.chat.domain.ChatMessageOutboxStatus;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageOutboxRepository extends JpaRepository<ChatMessageOutbox, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE ChatMessageOutbox o
           SET o.status = :processingStatus,
               o.processingToken = :processingToken,
               o.processingStartedAt = :now,
               o.attemptCount = o.attemptCount + 1,
               o.updatedAt = :now
           WHERE o.id = :outboxId
             AND o.status = :pendingStatus
             AND o.nextAttemptAt <= :now
             AND o.attemptCount < :maxAttempts
           """)
    int claim(
            @Param("outboxId") long outboxId,
            @Param("processingToken") String processingToken,
            @Param("now") ZonedDateTime now,
            @Param("maxAttempts") int maxAttempts,
            @Param("pendingStatus") ChatMessageOutboxStatus pendingStatus,
            @Param("processingStatus") ChatMessageOutboxStatus processingStatus
    );

    @EntityGraph(attributePaths = {
            "chatMessage",
            "chatMessage.chatRoom",
            "chatMessage.chatAttachments"
    })
    @Query("SELECT o FROM ChatMessageOutbox o WHERE o.id = :outboxId")
    Optional<ChatMessageOutbox> findWithMessageById(@Param("outboxId") long outboxId);

    @Query("""
           SELECT o.id FROM ChatMessageOutbox o
           WHERE o.status = :status
             AND o.nextAttemptAt <= :now
             AND o.attemptCount < :maxAttempts
           ORDER BY o.nextAttemptAt, o.id
           """)
    List<Long> findDispatchableIds(
            @Param("status") ChatMessageOutboxStatus status,
            @Param("now") ZonedDateTime now,
            @Param("maxAttempts") int maxAttempts,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE ChatMessageOutbox o
           SET o.status = :pendingStatus,
               o.processingToken = null,
               o.processingStartedAt = null,
               o.nextAttemptAt = :now,
               o.updatedAt = :now
           WHERE o.status = :processingStatus
             AND o.processingStartedAt < :cutoff
             AND o.attemptCount < :maxAttempts
           """)
    int recoverRetryableStaleProcessing(
            @Param("processingStatus") ChatMessageOutboxStatus processingStatus,
            @Param("pendingStatus") ChatMessageOutboxStatus pendingStatus,
            @Param("cutoff") ZonedDateTime cutoff,
            @Param("now") ZonedDateTime now,
            @Param("maxAttempts") int maxAttempts
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE ChatMessageOutbox o
           SET o.status = :failedStatus,
               o.processingToken = null,
               o.processingStartedAt = null,
               o.nextAttemptAt = null,
               o.updatedAt = :now
           WHERE o.status = :processingStatus
             AND o.processingStartedAt < :cutoff
             AND o.attemptCount >= :maxAttempts
           """)
    int failExhaustedStaleProcessing(
            @Param("processingStatus") ChatMessageOutboxStatus processingStatus,
            @Param("failedStatus") ChatMessageOutboxStatus failedStatus,
            @Param("cutoff") ZonedDateTime cutoff,
            @Param("now") ZonedDateTime now,
            @Param("maxAttempts") int maxAttempts
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE ChatMessageOutbox o
           SET o.status = :completedStatus,
               o.completionType = :completionType,
               o.processingToken = null,
               o.processingStartedAt = null,
               o.nextAttemptAt = null,
               o.completedAt = :completedAt,
               o.lastError = null,
               o.updatedAt = :completedAt
           WHERE o.id = :outboxId
             AND o.status = :processingStatus
             AND o.processingToken = :processingToken
           """)
    int markCompleted(
            @Param("outboxId") long outboxId,
            @Param("processingToken") String processingToken,
            @Param("processingStatus") ChatMessageOutboxStatus processingStatus,
            @Param("completedStatus") ChatMessageOutboxStatus completedStatus,
            @Param("completionType") ChatMessageOutboxCompletionType completionType,
            @Param("completedAt") ZonedDateTime completedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE ChatMessageOutbox o
           SET o.status = :nextStatus,
               o.processingToken = null,
               o.processingStartedAt = null,
               o.nextAttemptAt = :nextAttemptAt,
               o.lastError = :lastError,
               o.updatedAt = :failedAt
           WHERE o.id = :outboxId
             AND o.status = :processingStatus
             AND o.processingToken = :processingToken
           """)
    int recordFailure(
            @Param("outboxId") long outboxId,
            @Param("processingToken") String processingToken,
            @Param("processingStatus") ChatMessageOutboxStatus processingStatus,
            @Param("nextStatus") ChatMessageOutboxStatus nextStatus,
            @Param("nextAttemptAt") ZonedDateTime nextAttemptAt,
            @Param("lastError") String lastError,
            @Param("failedAt") ZonedDateTime failedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE ChatMessageOutbox o
           SET o.status = :completedStatus,
               o.completionType = :completionType,
               o.processingToken = null,
               o.processingStartedAt = null,
               o.nextAttemptAt = null,
               o.completedAt = :completedAt,
               o.lastError = null,
               o.updatedAt = :completedAt
           WHERE o.recipient.id = :recipientParticipantId
             AND o.chatMessage.id IN :messageIds
             AND o.status IN :completableStatuses
           """)
    int markHistoryServed(
            @Param("recipientParticipantId") long recipientParticipantId,
            @Param("messageIds") List<Long> messageIds,
            @Param("completableStatuses") List<ChatMessageOutboxStatus> completableStatuses,
            @Param("completedStatus") ChatMessageOutboxStatus completedStatus,
            @Param("completionType") ChatMessageOutboxCompletionType completionType,
            @Param("completedAt") ZonedDateTime completedAt
    );
}
