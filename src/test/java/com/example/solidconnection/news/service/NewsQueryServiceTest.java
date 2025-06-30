package com.example.solidconnection.news.service;

import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.NewsResponse;
import com.example.solidconnection.news.dto.NewsListResponse;
import com.example.solidconnection.news.fixture.NewsFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
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
    private SiteUserFixture siteUserFixture;

    @Autowired
    private NewsFixture newsFixture;

    @Test
    void 특정_사용자의_소식지_목록을_성공적으로_조회한다() {
        // given
        SiteUser user1 = siteUserFixture.멘토(1, "mentor1");
        SiteUser user2 = siteUserFixture.멘토(2, "mentor2");
        News news1 = newsFixture.소식지(user1.getId());
        News news2 = newsFixture.소식지(user1.getId());
        newsFixture.소식지(user2.getId());
        List<News> newsList = List.of(news1, news2);

        // when
        NewsListResponse response = newsQueryService.findNewsBySiteUserId(user1.getId());

        // then
        assertThat(response.newsResponseList()).hasSize(newsList.size());
        assertThat(response.newsResponseList())
                .extracting(NewsResponse::updatedAt)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }
}
