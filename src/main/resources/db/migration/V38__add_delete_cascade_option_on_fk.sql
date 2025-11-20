ALTER TABLE interested_country DROP FOREIGN KEY FK26u5am55jefclcd7r5smk8ai7;
ALTER TABLE interested_country
    ADD CONSTRAINT fk_interested_country_site_user_id
        FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE interested_region DROP FOREIGN KEY FKia6h0pbisqhgm3lkeya6vqo4w;
ALTER TABLE interested_region
    ADD CONSTRAINT fk_interested_region_site_user_id
        FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE comment DROP FOREIGN KEY FK11tfff2an5hdv747cktxbdi6t;
ALTER TABLE comment
    ADD CONSTRAINT fk_comment_site_user_id
        FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE post DROP FOREIGN KEY FKfu9q9o3mlqkd58wg45ykgu8ni;
ALTER TABLE post
    ADD CONSTRAINT fk_post_site_user_id
        FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE post_like DROP FOREIGN KEY FKgx1v0whinnoqveopoh6tb4ykb;
ALTER TABLE post_like
    ADD CONSTRAINT fk_post_like_site_user_id
        FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE liked_university_info_for_apply DROP FOREIGN KEY FKkuqxb64dnfrl7har8t5ionw83;
ALTER TABLE liked_university_info_for_apply
    ADD CONSTRAINT fk_liked_university_info_for_apply_site_user_id
        FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE application DROP FOREIGN KEY fk_app_site_user;
ALTER TABLE application
    ADD CONSTRAINT fk_app_site_user
        FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE gpa_score DROP FOREIGN KEY FK2k65qncfxvol5j4l4hb7d6iv1;
ALTER TABLE gpa_score
    ADD CONSTRAINT fk_gpa_score_site_user_id
        FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE language_test_score DROP FOREIGN KEY FKt2uevj2r4iuxumblj5ofbgmqn;
ALTER TABLE language_test_score
    ADD CONSTRAINT fk_language_test_score_site_user_id
        FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE mentor DROP FOREIGN KEY fk_mentor_site_user_id;
ALTER TABLE mentor
    ADD CONSTRAINT fk_mentor_site_user_id
        FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE mentoring DROP FOREIGN KEY fk_mentoring_site_user_id;
ALTER TABLE mentoring
    ADD CONSTRAINT fk_mentoring_site_user_id
        FOREIGN KEY (mentee_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE news DROP FOREIGN KEY fk_news_site_user_id;
ALTER TABLE news
    ADD CONSTRAINT fk_news_site_user_id
        FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE liked_news DROP FOREIGN KEY fk_liked_news_site_user_id;
ALTER TABLE liked_news
    ADD CONSTRAINT fk_liked_news_site_user_id
        FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE chat_participant DROP FOREIGN KEY FK_CHAT_PARTICIPANT_SITE_USER_ID;
ALTER TABLE chat_participant
    ADD CONSTRAINT fk_chat_participant_site_user_id
        FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE report DROP FOREIGN KEY fk_report_reporter_id;
ALTER TABLE report
    ADD CONSTRAINT fk_report_reporter_id
        FOREIGN KEY (reporter_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE user_block DROP FOREIGN KEY fk_user_block_blocker_id;
ALTER TABLE user_block
    ADD CONSTRAINT fk_user_block_blocker_id
        FOREIGN KEY (blocker_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE user_block DROP FOREIGN KEY fk_user_block_blocked_id;
ALTER TABLE user_block
    ADD CONSTRAINT fk_user_block_blocked_id
        FOREIGN KEY (blocked_id) REFERENCES site_user (id) ON DELETE CASCADE;

ALTER TABLE mentor_application DROP FOREIGN KEY fk_mentor_application_site_user;
ALTER TABLE mentor_application
    ADD CONSTRAINT fk_mentor_application_site_user
        FOREIGN KEY (site_user_id) REFERENCES site_user (id) ON DELETE CASCADE;
