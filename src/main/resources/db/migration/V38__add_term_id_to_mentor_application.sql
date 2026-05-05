ALTER TABLE mentor_application
    ADD COLUMN term_id BIGINT NOT NULL;

ALTER TABLE mentor_application
    ADD CONSTRAINT fk_mentor_application_term_id
        FOREIGN KEY (term_id) REFERENCES term(id);