package com.example.solidconnection.news.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.NewsCreateRequest;
import com.example.solidconnection.news.dto.NewsResponse;
import com.example.solidconnection.news.fixture.NewsFixture;
import com.example.solidconnection.news.repository.NewsRepository;
import com.example.solidconnection.s3.domain.ImgType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static com.example.solidconnection.common.exception.ErrorCode.NEWS_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.NEWS_TITLE_EMPTY;
import static com.example.solidconnection.common.exception.ErrorCode.NEWS_URL_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@TestContainerSpringBootTest
@DisplayName("소식지 서비스 테스트")
public class NewsServiceTest {

    @Autowired
    private NewsService newsService;

    @MockBean
    private S3Service s3Service;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private NewsFixture newsFixture;

    @Nested
    class 소식지_생성_테스트 {

        @Test
        void 소식지를_성공적으로_생성한다() {
            // given
            NewsCreateRequest request = createNewsCreateRequest();
            MultipartFile imageFile = createImageFile();
            String expectedImageUrl = "news/5a02ba2f-38f5-4ae9-9a24-53d624a18233";
            given(s3Service.uploadFile(any(), eq(ImgType.NEWS)))
                    .willReturn(new UploadedFileUrlResponse(expectedImageUrl));

            // when
            NewsResponse response = newsService.createNews(request, imageFile);

            // then
            News savedNews = newsRepository.findById(response.id()).orElseThrow();
            assertThat(response.id()).isEqualTo(savedNews.getId());
        }
    }

    @Nested
    class 소식지_수정_테스트 {

        private News originNews;

        @BeforeEach
        void setUp() {
            originNews = newsFixture.소식지();
        }

        @Test
        void 소식지를_성공적으로_수정한다() {
            // given
            String expectedTitle = "제목 수정";
            String expectedDescription = "설명 수정";
            String expectedUrl = "https://youtu.be/test-edit";
            MultipartFile expectedFile = createImageFile();
            String expectedNewImageUrl = "news/5a02ba2f-38f5-4ae9-9a24-53d624a18233-edit";
            given(s3Service.uploadFile(any(), eq(ImgType.NEWS)))
                    .willReturn(new UploadedFileUrlResponse(expectedNewImageUrl));

            // when
            NewsResponse response = newsService.updateNews(
                    originNews.getId(),
                    expectedTitle,
                    expectedDescription,
                    expectedUrl,
                    expectedFile
            );

            // then
            News savedNews = newsRepository.findById(response.id()).orElseThrow();
            assertThat(savedNews.getTitle()).isEqualTo(expectedTitle);
            assertThat(savedNews.getDescription()).isEqualTo(expectedDescription);
            assertThat(savedNews.getThumbnailUrl()).isEqualTo(expectedNewImageUrl);
            assertThat(savedNews.getUrl()).isEqualTo(expectedUrl);
        }

        @Test
        void 소식지_제목만_수정한다() {
            // given
            String expectedTitle = "제목 수정";
            String originalDescription = originNews.getDescription();
            String originalUrl = originNews.getUrl();
            String originalThumbnailUrl = originNews.getThumbnailUrl();

            // when
            NewsResponse response = newsService.updateNews(
                    originNews.getId(), expectedTitle, null, null, null
            );

            // then
            News savedNews = newsRepository.findById(response.id()).orElseThrow();
            assertThat(savedNews.getTitle()).isEqualTo(expectedTitle);
            assertThat(savedNews.getDescription()).isEqualTo(originalDescription);
            assertThat(savedNews.getUrl()).isEqualTo(originalUrl);
            assertThat(savedNews.getThumbnailUrl()).isEqualTo(originalThumbnailUrl);
        }

        @Test
        void 존재하지_않는_소식지_수정시_예외가_발생한다() {
            // given
            long invalidNewsId = 9999L;

            // when & then
            assertThatCode(() -> newsService.updateNews(
                    invalidNewsId,
                    "제목 수정",
                    null,
                    null,
                    null))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(NEWS_NOT_FOUND.getMessage());
        }

        @Test
        void 빈_제목으로_수정시_예외가_발생한다() {
            // when & then
            assertThatCode(() -> newsService.updateNews(
                    originNews.getId(),
                    "   ",
                    null,
                    null,
                    null))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(NEWS_TITLE_EMPTY.getMessage());
        }

        @Test
        void 잘못된_URL_형식으로_수정시_예외가_발생한다() {
            // when & then
            assertThatCode(() -> newsService.updateNews(
                    originNews.getId(),
                    null,
                    null,
                    "invalid-url",
                    null))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(NEWS_URL_INVALID.getMessage());
        }
    }

    private NewsCreateRequest createNewsCreateRequest() {
        return new NewsCreateRequest("제목", "설명", "https://youtu.be/test");
    }

    private MockMultipartFile createImageFile() {
        return new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }
}
