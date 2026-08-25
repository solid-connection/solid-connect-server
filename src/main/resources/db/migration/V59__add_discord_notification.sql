CREATE TABLE discord_notification (
    id BIGINT NOT NULL AUTO_INCREMENT,
    review_type VARCHAR(50) NOT NULL,
    review_id BIGINT NOT NULL,
    discord_channel_id VARCHAR(30) NOT NULL,
    discord_message_id VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_discord_notification PRIMARY KEY (id),
    CONSTRAINT uk_discord_notification_target UNIQUE (review_type, review_id)
);
