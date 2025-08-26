package com.example.solidconnection.news.fixture;

import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class NewsFixtureBuilder {

    private final NewsRepository newsRepository;

    private String title;
    private String description;
    private String thumbnailUrl;
    private String url;
    private long siteUserId;

    public NewsFixtureBuilder title(String title) {
        this.title = title;
        return this;
    }

    public NewsFixtureBuilder description(String description) {
        this.description = description;
        return this;
    }

    public NewsFixtureBuilder thumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }

    public NewsFixtureBuilder url(String url) {
        this.url = url;
        return this;
    }

    public NewsFixtureBuilder siteUserId(long siteUserId) {
        this.siteUserId = siteUserId;
        return this;
    }

    public News create() {
        News news = new News(
                title,
                description,
                thumbnailUrl,
                url,
                siteUserId);
        return newsRepository.save(news);
    }
}
