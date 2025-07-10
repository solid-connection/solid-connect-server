package com.example.solidconnection.news.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_liked_news_site_user_id_news_id",
                columnNames = {"site_user_id", "news_id"}
        )
})
public class LikedNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "news_id")
    private long newsId;

    @Column(name = "site_user_id")
    private long siteUserId;

    public LikedNews(long newsId, long siteUserId) {
        this.newsId = newsId;
        this.siteUserId = siteUserId;
    }
}
