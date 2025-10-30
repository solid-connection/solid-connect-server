-- 1. Term 테이블 생성
CREATE TABLE IF NOT EXISTS term (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    name VARCHAR(20) NOT NULL,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_term_name UNIQUE (name)
    );

-- 2. 기존에 사용하던 term 값들을 Term 테이블에 INSERT
INSERT IGNORE INTO term (name, is_current)
SELECT DISTINCT term, FALSE
FROM application
WHERE term NOT IN (SELECT name FROM term);

INSERT IGNORE INTO term (name, is_current)
SELECT DISTINCT term, FALSE
FROM mentor
WHERE term NOT IN (SELECT name FROM term);

INSERT IGNORE INTO term (name, is_current)
SELECT DISTINCT term, FALSE
FROM university_info_for_apply
WHERE term NOT IN (SELECT name FROM term);

-- 3. 현재 학기 설정
INSERT INTO term (name, is_current)
VALUES ('2026-1', TRUE)
    ON DUPLICATE KEY UPDATE is_current = TRUE;

-- 4. 각 테이블에 term_id 컬럼 추가 (임시로 nullable)
ALTER TABLE application
    ADD COLUMN term_id BIGINT NULL;

ALTER TABLE mentor
    ADD COLUMN term_id BIGINT NULL;

ALTER TABLE university_info_for_apply
    ADD COLUMN term_id BIGINT NULL;

-- 5. 기존 term(String) 값을 term_id(Long)로 매핑
UPDATE application a
    INNER JOIN term t ON a.term = t.name
    SET a.term_id = t.id
WHERE a.term_id IS NULL;

UPDATE mentor m
    INNER JOIN term t ON m.term = t.name
    SET m.term_id = t.id
WHERE m.term_id IS NULL;

UPDATE university_info_for_apply u
    INNER JOIN term t ON u.term = t.name
    SET u.term_id = t.id
WHERE u.term_id IS NULL;

-- 6. term_id를 NOT NULL로 변경
ALTER TABLE application
    MODIFY COLUMN term_id BIGINT NOT NULL;

ALTER TABLE mentor
    MODIFY COLUMN term_id BIGINT NOT NULL;

ALTER TABLE university_info_for_apply
    MODIFY COLUMN term_id BIGINT NOT NULL;

-- 7. term_id에 대해 FK 설정
ALTER TABLE application
    ADD CONSTRAINT fk_application_term_id
        FOREIGN KEY (term_id) REFERENCES term(id);

ALTER TABLE mentor
    ADD CONSTRAINT fk_mentor_term_id
        FOREIGN KEY (term_id) REFERENCES term(id);

ALTER TABLE university_info_for_apply
    ADD CONSTRAINT fk_university_info_for_apply_term_id
        FOREIGN KEY (term_id) REFERENCES term(id);

-- 8. term_id 기반 새로운 인덱스 생성
CREATE INDEX idx_app_user_term_id_delete
    ON application(site_user_id, term_id, is_delete);

CREATE INDEX idx_app_first_choice_term_id_search
    ON application(verify_status, term_id, is_delete, first_choice_university_info_for_apply_id);

CREATE INDEX idx_app_second_choice_term_id_search
    ON application(verify_status, term_id, is_delete, second_choice_university_info_for_apply_id);

CREATE INDEX idx_app_third_choice_term_id_search
    ON application(verify_status, term_id, is_delete, third_choice_university_info_for_apply_id);

-- 9. 기존 fk 삭제
SET @drop_fk1 = (
    SELECT CONCAT('ALTER TABLE `application` DROP FOREIGN KEY `', CONSTRAINT_NAME, '`;')
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'application'
      AND COLUMN_NAME = 'first_choice_university_info_for_apply_id'
      AND REFERENCED_TABLE_NAME = 'university_info_for_apply'
    LIMIT 1
);

SET @drop_fk1 = IFNULL(@drop_fk1, 'SELECT 1');
PREPARE stmt1 FROM @drop_fk1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SET @drop_fk2 = (
    SELECT CONCAT('ALTER TABLE `application` DROP FOREIGN KEY `', CONSTRAINT_NAME, '`;')
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'application'
      AND COLUMN_NAME = 'second_choice_university_info_for_apply_id'
      AND REFERENCED_TABLE_NAME = 'university_info_for_apply'
    LIMIT 1
);

SET @drop_fk2 = IFNULL(@drop_fk2, 'SELECT 1');
PREPARE stmt2 FROM @drop_fk2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

SET @drop_fk3 = (
    SELECT CONCAT('ALTER TABLE `application` DROP FOREIGN KEY `', CONSTRAINT_NAME, '`;')
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'application'
      AND COLUMN_NAME = 'third_choice_university_info_for_apply_id'
      AND REFERENCED_TABLE_NAME = 'university_info_for_apply'
    LIMIT 1
);

SET @drop_fk3 = IFNULL(@drop_fk3, 'SELECT 1');
PREPARE stmt3 FROM @drop_fk3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

SET @drop_fk4 = (
    SELECT CONCAT('ALTER TABLE `application` DROP FOREIGN KEY `', CONSTRAINT_NAME, '`;')
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'application'
      AND COLUMN_NAME = 'site_user_id'
      AND REFERENCED_TABLE_NAME = 'site_user'
    LIMIT 1
);

SET @drop_fk4 = IFNULL(@drop_fk4, 'SELECT 1');
PREPARE stmt4 FROM @drop_fk4;
EXECUTE stmt4;
DEALLOCATE PREPARE stmt4;

-- 10. 기존 term 관련 인덱스 삭제
DROP INDEX idx_app_first_choice_search ON application;
DROP INDEX idx_app_second_choice_search ON application;
DROP INDEX idx_app_third_choice_search ON application;
DROP INDEX idx_app_user_term_delete ON application;

-- 11. term 컬럼 삭제
ALTER TABLE application DROP COLUMN term;
ALTER TABLE mentor DROP COLUMN term;
ALTER TABLE university_info_for_apply DROP COLUMN term;

-- 12. FK 재생성
ALTER TABLE application
    ADD CONSTRAINT fk_application_first_choice_university_info_for_apply_id
        FOREIGN KEY (first_choice_university_info_for_apply_id)
            REFERENCES university_info_for_apply(id);

ALTER TABLE application
    ADD CONSTRAINT fk_application_second_choice_university_info_for_apply_id
        FOREIGN KEY (second_choice_university_info_for_apply_id)
            REFERENCES university_info_for_apply(id);

ALTER TABLE application
    ADD CONSTRAINT fk_application_third_choice_university_info_for_apply_id
        FOREIGN KEY (third_choice_university_info_for_apply_id)
            REFERENCES university_info_for_apply(id);

ALTER TABLE application
    ADD CONSTRAINT fk_app_site_user
        FOREIGN KEY (site_user_id)
            REFERENCES site_user(id);
