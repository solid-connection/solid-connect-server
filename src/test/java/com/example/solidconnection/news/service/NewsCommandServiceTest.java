package com.example.solidconnection.news.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.NewsCreateRequest;
import com.example.solidconnection.news.dto.NewsCommandResponse;
import com.example.solidconnection.news.dto.NewsUpdateRequest;
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

import static com.example.solidconnection.common.exception.ErrorCode.NEWS_TITLE_EMPTY;
import static com.example.solidconnection.common.exception.ErrorCode.NEWS_URL_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@TestContainerSpringBootTest
@DisplayName("소식지 생성/수정/삭제 서비스 테스트")
class NewsCommandServiceTest {

    @Autowired
    private NewsCommandService newsCommandService;

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
            NewsCommandResponse response = newsCommandService.createNews(request, imageFile);

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
            NewsUpdateRequest request = createNewsUpdateRequest(expectedTitle, expectedDescription, expectedUrl);

            // when
            NewsCommandResponse response = newsCommandService.updateNews(originNews.getId(), request, expectedFile
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
            NewsUpdateRequest request = createNewsUpdateRequest(expectedTitle, null, null);

            // when
            NewsCommandResponse response = newsCommandService.updateNews(originNews.getId(), request, null);

            // then
            News savedNews = newsRepository.findById(response.id()).orElseThrow();
            assertThat(savedNews.getTitle()).isEqualTo(expectedTitle);
            assertThat(savedNews.getDescription()).isEqualTo(originalDescription);
            assertThat(savedNews.getUrl()).isEqualTo(originalUrl);
            assertThat(savedNews.getThumbnailUrl()).isEqualTo(originalThumbnailUrl);
        }

        @Test
        void 빈_제목으로_수정시_예외가_발생한다() {
            // given
            NewsUpdateRequest request = createNewsUpdateRequest("   ", null, null);

            // when & then
            assertThatCode(() -> newsCommandService.updateNews(
                    originNews.getId(),
                    request,
                    null))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(NEWS_TITLE_EMPTY.getMessage());
        }

        @Test
        void 잘못된_URL_형식으로_수정시_예외가_발생한다() {
            // given
            NewsUpdateRequest request = createNewsUpdateRequest(null, null, "invalid-url");

            // when & then
            assertThatCode(() -> newsCommandService.updateNews(
                    originNews.getId(),
                    request,
                    null))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(NEWS_URL_INVALID.getMessage());
        }
    }

    @Nested
    class 소식지_삭제_테스트 {

        @Test
        void 소식지를_성공적으로_삭제한다() {
            // given
            News originNews = newsFixture.소식지();
            String expectedImageUrl = originNews.getThumbnailUrl();

            // when
            NewsCommandResponse response = newsCommandService.deleteNewsById(originNews.getId());

            // then
            assertThat(response.id()).isEqualTo(originNews.getId());
            assertThat(newsRepository.findById(originNews.getId())).isEmpty();
            then(s3Service).should().deletePostImage(expectedImageUrl);
        }
    }

    private NewsCreateRequest createNewsCreateRequest() {
        return new NewsCreateRequest("제목", "설명", "https://youtu.be/test");
    }

    private NewsUpdateRequest createNewsUpdateRequest(String title, String description, String url) {
        return new NewsUpdateRequest(title, description, url);
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
