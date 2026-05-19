ALTER TABLE site_user
    ADD COLUMN home_university_id BIGINT;

ALTER TABLE site_user
    ADD CONSTRAINT fk_site_user_home_university
        FOREIGN KEY (home_university_id) REFERENCES home_university(id) ON DELETE NO ACTION;