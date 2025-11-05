-- Mentor 테이블에 MentorStatus 추가
-- 1) mentor_status 컬럼 생성 (임시로 NULL 허용)
ALTER TABLE mentor
    ADD COLUMN mentor_status ENUM('TEMPORARY','APPROVED') NULL;

-- 2) 기존 행 모두 APPROVED로 백필
UPDATE mentor
SET mentor_status = 'APPROVED'
WHERE mentor_status IS NULL;

-- 3) NOT NULL + 기본값 TEMPORARY 로 고정
ALTER TABLE mentor
    MODIFY COLUMN mentor_status ENUM('TEMPORARY','APPROVED') NOT NULL DEFAULT 'TEMPORARY';

-- Role 에 TEMP_MENTOR 추가
ALTER TABLE site_user
    MODIFY COLUMN `role` ENUM('MENTEE','MENTOR','ADMIN','TEMP_MENTOR') NOT NULL;
