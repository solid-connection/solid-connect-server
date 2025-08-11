ALTER TABLE chat_room
    ADD COLUMN mentoring_id BIGINT,
ADD CONSTRAINT uk_chat_room_mentoring_id UNIQUE (mentoring_id),
ADD CONSTRAINT fk_chat_room_mentoring_id FOREIGN KEY (mentoring_id) REFERENCES mentoring(id);

