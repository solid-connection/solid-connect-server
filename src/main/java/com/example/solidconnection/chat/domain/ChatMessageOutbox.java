package com.example.solidconnection.chat.domain;

import com.example.solidconnection.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "chat_message_outbox",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chat_message_outbox_message_recipient",
                columnNames = {"chat_message_id", "recipient_chat_participant_id"}
        ),
        indexes = {
                @Index(
                        name = "idx_chat_message_outbox_pending",
                        columnList = "status, next_attempt_at"
                ),
                @Index(
                        name = "idx_chat_message_outbox_processing",
                        columnList = "status, processing_started_at"
                )
        }
)
public class ChatMessageOutbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_message_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChatMessage chatMessage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_chat_participant_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChatParticipant recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChatMessageOutboxStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "completion_type", nullable = true, length = 30)
    private ChatMessageOutboxCompletionType completionType;

    @ColumnDefault("0")
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "processing_token", nullable = true, length = 36)
    private String processingToken;

    @Column(name = "processing_started_at", nullable = true)
    private ZonedDateTime processingStartedAt;

    @Column(name = "next_attempt_at", nullable = true)
    private ZonedDateTime nextAttemptAt;

    @Column(name = "last_error", nullable = true, length = 1000)
    private String lastError;

    @Column(name = "completed_at", nullable = true)
    private ZonedDateTime completedAt;

    public ChatMessageOutbox(
            ChatMessage chatMessage,
            ChatParticipant recipient,
            ZonedDateTime nextAttemptAt
    ) {
        this.chatMessage = chatMessage;
        this.recipient = recipient;
        this.status = ChatMessageOutboxStatus.PENDING;
        this.nextAttemptAt = nextAttemptAt;
    }
}
