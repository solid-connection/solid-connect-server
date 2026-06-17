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
        FOREIGN KEY (application_id) REFERENCES application (id),
    CONSTRAINT fk_app_choice_univ_apply_info
        FOREIGN KEY (univ_apply_info_id) REFERENCES university_info_for_apply (id)
);

CREATE INDEX idx_app_choice_univ_apply_info_id ON application_choice (univ_apply_info_id);

ALTER TABLE application
    MODIFY COLUMN first_choice_university_info_for_apply_id BIGINT NULL;

ALTER TABLE host_university
    MODIFY COLUMN english_name VARCHAR(200) NOT NULL;

ALTER TABLE university_info_for_apply
    MODIFY COLUMN details_for_language VARCHAR(2000),
    MODIFY COLUMN details_for_accommodation VARCHAR(2000),
    MODIFY COLUMN details_for_apply VARCHAR(3000),
    MODIFY COLUMN details_for_major VARCHAR(3000),
    MODIFY COLUMN details VARCHAR(3000);

ALTER TABLE application
    ADD CONSTRAINT uk_application_nickname_for_apply
        UNIQUE (nickname_for_apply);
