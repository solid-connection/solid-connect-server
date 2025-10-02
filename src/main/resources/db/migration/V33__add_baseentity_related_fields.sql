ALTER TABLE application
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE gpa
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE language_test
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE board
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE post_image
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE post_like
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE country
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE interested_country
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE region
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE interested_region
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE channel
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE mentor
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE mentoring
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE liked_news
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE report
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE site_user
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE language_requirement
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE liked_university_info_for_apply
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE university_info_for_apply
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);

ALTER TABLE university
    ADD COLUMN created_at DATETIME(6),
    ADD COLUMN updated_at DATETIME(6);
