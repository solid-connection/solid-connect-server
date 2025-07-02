ALTER TABLE news
    ADD COLUMN site_user_id BIGINT NOT NULL;
ALTER TABLE news
    ADD CONSTRAINT fk_news_site_user_id FOREIGN KEY (site_user_id) REFERENCES site_user (id);
