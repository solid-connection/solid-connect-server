ALTER TABLE term
    MODIFY COLUMN is_current BOOLEAN NULL DEFAULT NULL;

UPDATE term
SET is_current = NULL
WHERE is_current = FALSE;

ALTER TABLE term
    ADD CONSTRAINT uk_term_is_current UNIQUE (is_current);
