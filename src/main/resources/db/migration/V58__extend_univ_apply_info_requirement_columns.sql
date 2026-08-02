ALTER TABLE university_info_for_apply
    MODIFY COLUMN gpa_requirement VARCHAR(2000) NULL,
    MODIFY COLUMN semester_requirement VARCHAR(2000) NULL,
    MODIFY COLUMN details_for_language VARCHAR(4000) NULL;
