ALTER TABLE post ADD INDEX idx_post_board_code_category_created_at (board_code, category, created_at);
ALTER TABLE post ADD INDEX idx_post_board_code_created_at (board_code, created_at);
ALTER TABLE post_image ADD INDEX idx_post_image_post_id (post_id);
ALTER TABLE post_like ADD INDEX idx_post_like_post_id (post_id);

ALTER TABLE chat_message ADD INDEX idx_chat_message_room_created_at (chat_room_id, created_at);

ALTER TABLE gpa_score ADD INDEX idx_gpa_score_verify_status_created_at (verify_status, created_at);
ALTER TABLE language_test_score ADD INDEX idx_language_test_score_verify_status_created_at (verify_status, created_at);

ALTER TABLE site_user ADD INDEX idx_site_user_status_created_at (user_status, created_at);
ALTER TABLE report ADD INDEX idx_report_reported_id (reported_id);
