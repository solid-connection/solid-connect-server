ALTER TABLE site_user
ADD CONSTRAINT uk_site_user_nickname
UNIQUE (nickname);
