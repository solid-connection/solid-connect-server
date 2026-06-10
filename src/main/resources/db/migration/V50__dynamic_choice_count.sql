ALTER TABLE home_university
    ADD COLUMN max_choice_count INT NOT NULL DEFAULT 3,
    ADD CONSTRAINT chk_max_choice_count CHECK (max_choice_count >= 1);

CREATE TABLE application_choice
(
    application_id     BIGINT NOT NULL,
    choice_order       INT    NOT NULL,
    univ_apply_info_id BIGINT NOT NULL,
    PRIMARY KEY (application_id, choice_order),
    CONSTRAINT fk_app_choice_application
        FOREIGN KEY (application_id) REFERENCES application (id)
);

ALTER TABLE application
    MODIFY COLUMN first_choice_university_info_for_apply_id BIGINT NULL;
