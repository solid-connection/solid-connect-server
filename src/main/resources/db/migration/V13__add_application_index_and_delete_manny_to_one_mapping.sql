ALTER TABLE application RENAME COLUMN first_choice_university_id TO first_choice_university_info_for_apply_id;
ALTER TABLE application RENAME COLUMN second_choice_university_id TO second_choice_university_info_for_apply_id;
ALTER TABLE application RENAME COLUMN third_choice_university_id TO third_choice_university_info_for_apply_id;

CREATE INDEX idx_app_user_term_delete
    ON application(site_user_id, term, is_delete);

CREATE INDEX idx_app_first_choice_search
    ON application(verify_status, term, is_delete, first_choice_university_info_for_apply_id);

CREATE INDEX idx_app_second_choice_search
    ON application(verify_status, term, is_delete, second_choice_university_info_for_apply_id);

CREATE INDEX idx_app_third_choice_search
    ON application(verify_status, term, is_delete, third_choice_university_info_for_apply_id);
