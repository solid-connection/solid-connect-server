CREATE TABLE chat_room
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    is_group BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE TABLE chat_participant
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    site_user_id BIGINT NOT NULL,
    chat_room_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT FK_CHAT_PARTICIPANT_CHAT_ROOM_ID FOREIGN KEY (chat_room_id) REFERENCES chat_room (id),
    CONSTRAINT FK_CHAT_PARTICIPANT_SITE_USER_ID FOREIGN KEY (site_user_id) REFERENCES site_user (id),
    CONSTRAINT UK_CHAT_PARTICIPANT_CHAT_ROOM_ID_SITE_USER_ID UNIQUE (chat_room_id, site_user_id)
);

CREATE TABLE chat_message
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(500) NOT NULL,
    has_attachment BOOLEAN NOT NULL,
    sender_id BIGINT NOT NULL,
    chat_room_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT FK_CHAT_MESSAGE_CHAT_ROOM_ID FOREIGN KEY (chat_room_id) REFERENCES chat_room (id),
    CONSTRAINT FK_CHAT_MESSAGE_SENDER_ID FOREIGN KEY (sender_id) REFERENCES chat_participant (id)
);

CREATE TABLE chat_attachment
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    is_image BOOLEAN NOT NULL,
    url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    chat_message_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT FK_CHAT_ATTACHMENT_CHAT_MESSAGE_ID FOREIGN KEY (chat_message_id) REFERENCES chat_message (id)
);

CREATE TABLE chat_read_status
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_room_id BIGINT NOT NULL,
    chat_participant_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT FK_CHAT_READ_STATUS_CHAT_ROOM_ID FOREIGN KEY (chat_room_id) REFERENCES chat_room (id),
    CONSTRAINT FK_CHAT_READ_STATUS_CHAT_PARTICIPANT_ID FOREIGN KEY (chat_participant_id) REFERENCES chat_participant (id),
    CONSTRAINT UK_CHAT_READ_STATUS_CHAT_ROOM_ID_CHAT_PARTICIPANT_ID UNIQUE (chat_room_id, chat_participant_id)
);
