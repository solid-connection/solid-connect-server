package com.example.solidconnection.news.service;

import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.NewsFindResponse;
import com.example.solidconnection.news.dto.NewsItemResponse;
import com.example.solidconnection.news.dto.NewsResponse;
import com.example.solidconnection.news.fixture.NewsFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestContainerSpringBootTest
@DisplayName("소식지 조회 서비스 테스트")
class NewsQueryServiceTest {

    @Autowired
    private NewsQueryService newsQueryService;

    @Autowired
    private NewsFixture newsFixture;

    @Test
    void 소식지_목록을_성공적으로_조회한다() {
        // given
        News news1 = newsFixture.소식지();
        News news2 = newsFixture.소식지();
        News news3 = newsFixture.소식지();
        List<News> newsList = List.of(news1, news2, news3);

        // when
        NewsResponse response = newsQueryService.searchNews();

        // then
        assertThat(response.newsItemsResponseList()).hasSize(newsList.size());
        assertThat(response.newsItemsResponseList())
                .extracting(NewsItemResponse::updatedAt)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    void 소식지를_성공적으로_조회한다() {
        // given
        News news = newsFixture.소식지();

        // when
        NewsFindResponse response = newsQueryService.findNewsById(news.getId());

        // then
        assertThat(response.id()).isEqualTo(news.getId());
        assertThat(response.description()).isEqualTo(news.getDescription());
    }
}
