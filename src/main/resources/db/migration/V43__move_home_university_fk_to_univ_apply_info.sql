ALTER TABLE host_university
    DROP FOREIGN KEY fk_host_university_home_university;

ALTER TABLE host_university
    DROP COLUMN home_university_id;

ALTER TABLE university_info_for_apply
    ADD COLUMN home_university_id BIGINT NULL;

ALTER TABLE university_info_for_apply
    ADD CONSTRAINT fk_university_info_for_apply_home_university
        FOREIGN KEY (home_university_id) REFERENCES home_university (id) ON DELETE NO ACTION;
