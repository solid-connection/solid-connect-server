ALTER TABLE site_user
ADD COLUMN auth_type ENUM('KAKAO', 'APPLE', 'EMAIL');

UPDATE site_user
SET auth_type = 'KAKAO'
WHERE auth_type IS NULL;

ALTER TABLE site_user
MODIFY COLUMN auth_type ENUM('KAKAO', 'APPLE', 'EMAIL') NOT NULL;

ALTER TABLE site_user
ADD CONSTRAINT uk_site_user_email_auth_type
UNIQUE (email, auth_type);
