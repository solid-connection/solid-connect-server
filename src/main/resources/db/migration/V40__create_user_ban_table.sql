CREATE TABLE user_ban
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    banned_user_id  BIGINT       NOT NULL,
    duration        VARCHAR(30)  NOT NULL,
    expired_at      DATETIME(6)    NOT NULL,
    is_unbanned     TINYINT(1)   NOT NULL DEFAULT 0,
    unbanned_by     BIGINT       NULL,
    unbanned_at     DATETIME(6)    NULL,
    created_at      DATETIME(6)    NOT NULL,
    updated_at      DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_user_ban_banned_user_id FOREIGN KEY (banned_user_id) REFERENCES site_user (id),
    CONSTRAINT fk_user_ban_unbanned_by_id FOREIGN KEY (unbanned_by) REFERENCES site_user (id)
);

ALTER TABLE site_user
    ADD COLUMN user_status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE report
    ADD COLUMN reported_id BIGINT;
