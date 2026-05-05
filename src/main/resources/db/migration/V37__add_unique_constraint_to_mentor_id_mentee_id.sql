ALTER TABLE mentoring
ADD CONSTRAINT uk_mentoring_mentor_id_mentee_id
UNIQUE (mentor_id, mentee_id);
