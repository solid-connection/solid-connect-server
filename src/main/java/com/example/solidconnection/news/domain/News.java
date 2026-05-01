package com.example.solidconnection.news.domain;

import com.example.solidconnection.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class News extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "site_user_id")
    private long siteUserId;

    public News(
            String title,
            String description,
            String thumbnailUrl,
            String url,
            long siteUserId) {
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.url = url;
        this.siteUserId = siteUserId;
    }

    public void updateNews(String title, String description, String url) {
        this.title = title;
        this.description = description;
        this.url = url;
    }

    public void updateThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
