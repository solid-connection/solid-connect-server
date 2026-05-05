CREATE TABLE liked_news (
    id BIGINT NOT NULL AUTO_INCREMENT,
    news_id BIGINT NOT NULL,
    site_user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_liked_news_site_user_id_news_id UNIQUE (site_user_id, news_id),
    CONSTRAINT fk_liked_news_news_id FOREIGN KEY (news_id) REFERENCES news(id),
    CONSTRAINT fk_liked_news_site_user_id FOREIGN KEY (site_user_id) REFERENCES site_user(id)
);
