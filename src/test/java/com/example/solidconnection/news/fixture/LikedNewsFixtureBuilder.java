package com.example.solidconnection.news.fixture;

import com.example.solidconnection.news.domain.LikedNews;
import com.example.solidconnection.news.repository.LikedNewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class LikedNewsFixtureBuilder {

    private final LikedNewsRepository likedNewsRepository;

    private long newsId;

    private long siteUserId;

    public LikedNewsFixtureBuilder likedNews() {
        return new LikedNewsFixtureBuilder(likedNewsRepository);
    }

    public LikedNewsFixtureBuilder newsId(long newsId) {
        this.newsId = newsId;
        return this;
    }

    public LikedNewsFixtureBuilder siteUserId(long siteUserId) {
        this.siteUserId = siteUserId;
        return this;
    }

    public LikedNews create() {
        LikedNews likedNews = new LikedNews(newsId, siteUserId);
        return likedNewsRepository.save(likedNews);
    }
}
