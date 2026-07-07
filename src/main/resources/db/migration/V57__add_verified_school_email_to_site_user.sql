ALTER TABLE site_user
    ADD COLUMN verified_school_email VARCHAR(100) NULL,
    ADD CONSTRAINT uk_site_user_verified_school_email UNIQUE (verified_school_email);
