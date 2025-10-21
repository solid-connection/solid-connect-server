CREATE TABLE mentor_application
(
    id                        BIGINT       NOT NULL AUTO_INCREMENT,
    site_user_id              BIGINT,
    country_code              VARCHAR(255),
    university_id             BIGINT,
    university_select_type    enum ('CATALOG','OTHER') not null,
    mentor_proof_url          VARCHAR(500) NOT NULL,
    rejected_reason           VARCHAR(255),
    exchange_status           enum('AFTER_EXCHANGE','STUDYING_ABROAD') NOT NULL,
    mentor_application_status enum('APPROVED','PENDING','REJECTED') NOT NULL,
    created_at                DATETIME(6),
    updated_at                DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_mentor_application_site_user FOREIGN KEY (site_user_id) REFERENCES site_user (id),
    CONSTRAINT chk_ma_university_select_rule CHECK (
        (university_select_type = 'CATALOG' AND university_id IS NOT NULL) OR
        (university_select_type = 'OTHER'   AND university_id IS NULL)
    )
) ENGINE=InnoDB
