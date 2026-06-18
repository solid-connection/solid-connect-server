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
