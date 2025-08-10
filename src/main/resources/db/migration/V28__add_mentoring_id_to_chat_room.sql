ALTER TABLE chat_room
    ADD COLUMN mentoring_id BIGINT;

ALTER TABLE chat_room
    ADD CONSTRAINT uk_chat_room_mentoring_id
        UNIQUE (mentoring_id);
