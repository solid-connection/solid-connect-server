package com.example.solidconnection.news.fixture;

import com.example.solidconnection.news.domain.LikedNews;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class LikedNewsFixture {

    private final LikedNewsFixtureBuilder likedNewsFixtureBuilder;

    public LikedNews 소식지_좋아요(long newsId, long siteUserId) {
        return likedNewsFixtureBuilder.likedNews()
                .newsId(newsId)
                .siteUserId(siteUserId)
                .create();
    }
}
