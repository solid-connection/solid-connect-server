ALTER TABLE application
    DROP FOREIGN KEY fk_application_first_choice_university_info_for_apply_id,
    DROP FOREIGN KEY fk_application_second_choice_university_info_for_apply_id,
    DROP FOREIGN KEY fk_application_third_choice_university_info_for_apply_id;

DROP INDEX idx_app_first_choice_term_id_search ON application;
DROP INDEX idx_app_second_choice_term_id_search ON application;
DROP INDEX idx_app_third_choice_term_id_search ON application;

ALTER TABLE application
    DROP COLUMN first_choice_university_info_for_apply_id,
    DROP COLUMN second_choice_university_info_for_apply_id,
    DROP COLUMN third_choice_university_info_for_apply_id;
