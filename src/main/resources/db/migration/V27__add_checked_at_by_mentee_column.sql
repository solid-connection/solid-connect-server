ALTER TABLE mentoring
    RENAME COLUMN checked_at TO checked_at_by_mentor;

ALTER TABLE mentoring
    ADD COLUMN checked_at_by_mentee DATETIME(6) NULL;
