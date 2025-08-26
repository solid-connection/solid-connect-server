CREATE TABLE mentor
(
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    university_id BIGINT        NOT NULL,
    site_user_id  BIGINT        NOT NULL,
    mentee_count  INT           NOT NULL DEFAULT 0,
    has_badge     BOOLEAN       NOT NULL DEFAULT FALSE,
    introduction  VARCHAR(1000) NULL,
    pass_tip      VARCHAR(1000) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_mentor_university_id FOREIGN KEY (university_id) REFERENCES university (id),
    CONSTRAINT fk_mentor_site_user_id FOREIGN KEY (site_user_id) REFERENCES site_user (id)
);

CREATE TABLE mentoring
(
    id              BIGINT                                   NOT NULL AUTO_INCREMENT,
    mentor_id       BIGINT                                   NOT NULL,
    mentee_id       BIGINT                                   NOT NULL,
    created_at      DATETIME(6)                              NOT NULL,
    confirmed_at    DATETIME(6)                              NULL,
    checked_at      DATETIME(6)                              NULL,
    verify_status   ENUM ('PENDING', 'REJECTED', 'APPROVED') NOT NULL DEFAULT 'PENDING',
    rejected_reason VARCHAR(500)                             NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_mentoring_mentor_id FOREIGN KEY (mentor_id) REFERENCES mentor (id),
    CONSTRAINT fk_mentoring_site_user_id FOREIGN KEY (mentee_id) REFERENCES site_user (id)
);

CREATE TABLE channel
(
    id        BIGINT                                          NOT NULL AUTO_INCREMENT,
    mentor_id BIGINT                                          NOT NULL,
    sequence  INT                                             NOT NULL,
    type      ENUM ('BLOG', 'INSTAGRAM', 'YOUTUBE', 'BRUNCH') NOT NULL,
    url       VARCHAR(500)                                    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_channel_mentor_id FOREIGN KEY (mentor_id) REFERENCES mentor (id),
    CONSTRAINT uk_channel_mentor_id_sequence UNIQUE (mentor_id, sequence)
);
