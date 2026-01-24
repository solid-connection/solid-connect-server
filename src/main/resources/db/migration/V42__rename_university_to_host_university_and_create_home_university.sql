RENAME TABLE university TO host_university;

ALTER TABLE university_info_for_apply
    DROP FOREIGN KEY FKd0257hco6uy2utd1xccjh3fal;

ALTER TABLE university_info_for_apply
    ADD CONSTRAINT fk_university_info_for_apply_host_university
        FOREIGN KEY (university_id) REFERENCES host_university (id) ON DELETE NO ACTION;

CREATE TABLE IF NOT EXISTS home_university
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    name       VARCHAR(100)          NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT uk_home_university_name UNIQUE (name)
);

ALTER TABLE host_university
    ADD COLUMN home_university_id BIGINT NULL;

ALTER TABLE host_university
    ADD CONSTRAINT fk_host_university_home_university
        FOREIGN KEY (home_university_id) REFERENCES home_university (id) ON DELETE NO ACTION;
