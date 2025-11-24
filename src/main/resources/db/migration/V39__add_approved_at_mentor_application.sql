ALTER TABLE mentor_application
    ADD COLUMN approved_at DATETIME(6);

UPDATE mentor_application
SET approved_at = NOW()
WHERE mentor_application_status = 'APPROVED'
  AND approved_at IS NULL;