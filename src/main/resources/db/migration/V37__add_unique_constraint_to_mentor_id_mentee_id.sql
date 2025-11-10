ALTER TABLE mentoring
ADD CONSTRAINT uk_mentoring_mentor_id_mentee_id
UNIQUE (mentorId, menteeId);
