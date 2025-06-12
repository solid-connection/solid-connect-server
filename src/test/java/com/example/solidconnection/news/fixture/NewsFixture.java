package com.example.solidconnection.news.fixture;

import com.example.solidconnection.news.domain.News;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class NewsFixture {

    private final NewsFixtureBuilder newsFixtureBuilder;

    public News 소식지() {
        return newsFixtureBuilder
                .title("소식지 제목")
                .description("소식지 설명")
                .thumbnailUrl("news/5a02ba2f-38f5-4ae9-9a24-53d624a18233")
                .url("https://youtu.be/test")
                .create();
    }
}
