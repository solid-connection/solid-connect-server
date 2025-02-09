ALTER TABLE site_user
    modify ROLE enum ('MENTEE', 'MENTOR', 'ADMIN') NOT NULL;
