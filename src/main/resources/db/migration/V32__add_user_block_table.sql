CREATE TABLE user_block
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    blocker_id  BIGINT       NOT NULL,
    blocked_id  BIGINT       NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_block_blocker_id_blocked_id UNIQUE (blocker_id, blocked_id),
    CONSTRAINT fk_user_block_blocker_id FOREIGN KEY (blocker_id) REFERENCES site_user (id),
    CONSTRAINT fk_user_block_blocked_id FOREIGN KEY (blocked_id) REFERENCES site_user (id)
);
