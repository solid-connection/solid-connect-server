CREATE TABLE chat_message_outbox
(
    id                            BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_message_id               BIGINT        NOT NULL,
    recipient_chat_participant_id BIGINT        NOT NULL,
    status                        VARCHAR(20)    NOT NULL,
    completion_type               VARCHAR(30)    NULL,
    attempt_count                 INT            NOT NULL DEFAULT 0,
    processing_token              VARCHAR(36)    NULL,
    processing_started_at         DATETIME(6)    NULL,
    next_attempt_at               DATETIME(6)    NULL,
    last_error                    VARCHAR(1000)  NULL,
    completed_at                  DATETIME(6)    NULL,
    created_at                    DATETIME(6)    NOT NULL,
    updated_at                    DATETIME(6)    NOT NULL,
    CONSTRAINT uk_chat_message_outbox_message_recipient
        UNIQUE (chat_message_id, recipient_chat_participant_id),
    CONSTRAINT fk_chat_message_outbox_message
        FOREIGN KEY (chat_message_id) REFERENCES chat_message (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_message_outbox_recipient
        FOREIGN KEY (recipient_chat_participant_id) REFERENCES chat_participant (id) ON DELETE CASCADE,
    INDEX idx_chat_message_outbox_pending (status, next_attempt_at),
    INDEX idx_chat_message_outbox_processing (status, processing_started_at)
);
