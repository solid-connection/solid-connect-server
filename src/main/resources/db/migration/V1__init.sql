CREATE TABLE IF NOT EXISTS application
(
    id                          BIGINT AUTO_INCREMENT         NOT NULL,
    term                        VARCHAR(50)                   NOT NULL,
    site_user_id                BIGINT NULL,
    nickname_for_apply          VARCHAR(100) NULL,
    update_count                INT         DEFAULT 0         NOT NULL,
    verify_status               VARCHAR(50) DEFAULT 'PENDING' NOT NULL,
    gpa DOUBLE NOT NULL,
    gpa_criteria DOUBLE NOT NULL,
    language_test_type          ENUM                          NOT NULL,
    language_test_score         VARCHAR(255)                  NOT NULL,
    gpa_report_url              VARCHAR(500)                  NOT NULL,
    language_test_report_url    VARCHAR(500)                  NOT NULL,
    first_choice_university_id  BIGINT NULL,
    second_choice_university_id BIGINT NULL,
    third_choice_university_id  BIGINT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS board
(
    code        VARCHAR(20) NOT NULL,
    korean_name VARCHAR(20) NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (code)
);

CREATE TABLE IF NOT EXISTS comment
(
    created_at   datetime NULL,
    id           BIGINT AUTO_INCREMENT NOT NULL,
    parent_id    BIGINT NULL,
    post_id      BIGINT NULL,
    site_user_id BIGINT NULL,
    updated_at   datetime NULL,
    content      VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS country
(
    code        VARCHAR(2)   NOT NULL,
    region_code VARCHAR(10) NULL,
    korean_name VARCHAR(100) NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (code)
);

CREATE TABLE IF NOT EXISTS interested_country
(
    country_code VARCHAR(2) NULL,
    id           BIGINT AUTO_INCREMENT NOT NULL,
    site_user_id BIGINT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS interested_region
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    site_user_id BIGINT NULL,
    region_code  VARCHAR(10) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS language_requirement
(
    id                           BIGINT AUTO_INCREMENT NOT NULL,
    university_info_for_apply_id BIGINT NULL,
    language_test_type           ENUM         NOT NULL,
    min_score                    VARCHAR(255) NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS liked_university
(
    id                           BIGINT AUTO_INCREMENT NOT NULL,
    site_user_id                 BIGINT NULL,
    university_info_for_apply_id BIGINT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS post
(
    is_question  BIT(1) NULL,
    created_at   datetime NULL,
    id           BIGINT AUTO_INCREMENT NOT NULL,
    like_count   BIGINT NULL,
    site_user_id BIGINT NULL,
    updated_at   datetime NULL,
    view_count   BIGINT NULL,
    board_code   VARCHAR(20) NULL,
    content      VARCHAR(1000) NULL,
    category     ENUM NULL,
    title        VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS post_image
(
    id      BIGINT AUTO_INCREMENT NOT NULL,
    post_id BIGINT NULL,
    url     VARCHAR(500) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS post_like
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    post_id      BIGINT NULL,
    site_user_id BIGINT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS region
(
    code        VARCHAR(10)  NOT NULL,
    korean_name VARCHAR(100) NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (code)
);

CREATE TABLE IF NOT EXISTS site_user
(
    quited_at            date NULL,
    id                   BIGINT AUTO_INCREMENT NOT NULL,
    nickname_modified_at datetime NULL,
    birth                VARCHAR(20)  NOT NULL,
    email                VARCHAR(100) NOT NULL,
    nickname             VARCHAR(100) NOT NULL,
    profile_image_url    VARCHAR(500) NULL,
    gender               ENUM         NOT NULL,
    preparation_stage    ENUM         NOT NULL,
    `role`               ENUM         NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS university
(
    id                   BIGINT AUTO_INCREMENT NOT NULL,
    region_code          VARCHAR(10) NULL,
    country_code         VARCHAR(2) NULL,
    format_name          VARCHAR(100) NOT NULL,
    english_name         VARCHAR(100) NOT NULL,
    korean_name          VARCHAR(100) NOT NULL,
    background_image_url VARCHAR(500) NOT NULL,
    logo_image_url       VARCHAR(500) NOT NULL,
    details_for_local    VARCHAR(1000) NULL,
    homepage_url         VARCHAR(500) NULL,
    english_course_url   VARCHAR(500) NULL,
    accommodation_url    VARCHAR(500) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS university_info_for_apply
(
    id                              BIGINT AUTO_INCREMENT NOT NULL,
    term                            VARCHAR(50)  NOT NULL,
    university_id                   BIGINT NULL,
    korean_name                     VARCHAR(100) NOT NULL,
    student_capacity                INT NULL,
    tuition_fee_type                ENUM NULL,
    semester_available_for_dispatch ENUM NULL,
    details_for_language            VARCHAR(1000) NULL,
    gpa_requirement                 VARCHAR(100) NULL,
    gpa_requirement_criteria        VARCHAR(100) NULL,
    semester_requirement            VARCHAR(100) NULL,
    details_for_apply               VARCHAR(1000) NULL,
    details_for_major               VARCHAR(1000) NULL,
    details_for_english_course      VARCHAR(1000) NULL,
    details_for_accommodation       VARCHAR(1000) NULL,
    details                         VARCHAR(500) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

ALTER TABLE site_user
    ADD CONSTRAINT site_user_email_unique UNIQUE (email);

ALTER TABLE comment
    ADD CONSTRAINT FK11tfff2an5hdv747cktxbdi6t FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE NO ACTION;

CREATE INDEX FK11tfff2an5hdv747cktxbdi6t ON comment (site_user_id);

ALTER TABLE interested_country
    ADD CONSTRAINT FK26u5am55jefclcd7r5smk8ai7 FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE NO ACTION;

CREATE INDEX FK26u5am55jefclcd7r5smk8ai7 ON interested_country (site_user_id);

ALTER TABLE interested_region
    ADD CONSTRAINT FK7h2182pqkavi9d8o2pku6gidi FOREIGN KEY (region_code) REFERENCES region (code) ON DELETE NO ACTION;

CREATE INDEX FK7h2182pqkavi9d8o2pku6gidi ON interested_region (region_code);

ALTER TABLE interested_country
    ADD CONSTRAINT FK7x4ad24lblkq2ss0920uqfd6s FOREIGN KEY (country_code) REFERENCES country (code) ON DELETE NO ACTION;

CREATE INDEX FK7x4ad24lblkq2ss0920uqfd6s ON interested_country (country_code);

ALTER TABLE university_info_for_apply
    ADD CONSTRAINT FKd0257hco6uy2utd1xccjh3fal FOREIGN KEY (university_id) REFERENCES university (id) ON DELETE NO ACTION;

CREATE INDEX FKd0257hco6uy2utd1xccjh3fal ON university_info_for_apply (university_id);

ALTER TABLE post
    ADD CONSTRAINT FKfu9q9o3mlqkd58wg45ykgu8ni FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE NO ACTION;

CREATE INDEX FKfu9q9o3mlqkd58wg45ykgu8ni ON post (site_user_id);

ALTER TABLE post_like
    ADD CONSTRAINT FKgx1v0whinnoqveopoh6tb4ykb FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE NO ACTION;

CREATE INDEX FKgx1v0whinnoqveopoh6tb4ykb ON post_like (site_user_id);

ALTER TABLE liked_university
    ADD CONSTRAINT FKhj3gn3mqmfeiiw9jt83g7t3rk FOREIGN KEY (university_info_for_apply_id) REFERENCES university_info_for_apply (id) ON DELETE NO ACTION;

CREATE INDEX FKhj3gn3mqmfeiiw9jt83g7t3rk_idx ON liked_university (university_info_for_apply_id);

ALTER TABLE interested_region
    ADD CONSTRAINT FKia6h0pbisqhgm3lkeya6vqo4w FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE NO ACTION;

CREATE INDEX FKia6h0pbisqhgm3lkeya6vqo4w ON interested_region (site_user_id);

ALTER TABLE country
    ADD CONSTRAINT FKife035f2scmgcutdtv6bfd6g8 FOREIGN KEY (region_code) REFERENCES region (code) ON DELETE NO ACTION;

CREATE INDEX FKife035f2scmgcutdtv6bfd6g8 ON country (region_code);

ALTER TABLE university
    ADD CONSTRAINT FKksoyt17h0te1ra588y4a3208r FOREIGN KEY (country_code) REFERENCES country (code) ON DELETE NO ACTION;

CREATE INDEX FKksoyt17h0te1ra588y4a3208r ON university (country_code);

ALTER TABLE university
    ADD CONSTRAINT FKpwr8ocev54r8d22wdyj4a37bc FOREIGN KEY (region_code) REFERENCES region (code) ON DELETE NO ACTION;

CREATE INDEX FKpwr8ocev54r8d22wdyj4a37bc ON university (region_code);

ALTER TABLE language_requirement
    ADD CONSTRAINT FKr75pgslwfbrvjkfau6dwtlg8l FOREIGN KEY (university_info_for_apply_id) REFERENCES university_info_for_apply (id) ON DELETE NO ACTION;

CREATE INDEX FKr75pgslwfbrvjkfau6dwtlg8l ON language_requirement (university_info_for_apply_id);

ALTER TABLE liked_university
    ADD CONSTRAINT FKrrhud921brslcukx6fyuh0th3 FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE NO ACTION;

CREATE INDEX FKrrhud921brslcukx6fyuh0th3 ON liked_university (site_user_id);

ALTER TABLE application
    ADD CONSTRAINT FKs4s3hebtn7vwd0b4xt8msxsis FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE NO ACTION;

CREATE INDEX FKs4s3hebtn7vwd0b4xt8msxsis ON application (site_user_id);

ALTER TABLE application
    ADD CONSTRAINT fk_university_info_for_apply_id_1 FOREIGN KEY (first_choice_university_id) REFERENCES university_info_for_apply (id) ON DELETE NO ACTION;

CREATE INDEX fk_university_info_for_apply_id_1 ON application (first_choice_university_id);

ALTER TABLE application
    ADD CONSTRAINT fk_university_info_for_apply_id_2 FOREIGN KEY (second_choice_university_id) REFERENCES university_info_for_apply (id) ON DELETE NO ACTION;

CREATE INDEX fk_university_info_for_apply_id_2 ON application (second_choice_university_id);