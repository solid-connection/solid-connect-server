package com.example.solidconnection.news.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.NewsCommandResponse;
import com.example.solidconnection.news.dto.NewsCreateRequest;
import com.example.solidconnection.news.dto.NewsUpdateRequest;
import com.example.solidconnection.news.fixture.NewsFixture;
import com.example.solidconnection.news.repository.NewsRepository;
import com.example.solidconnection.s3.domain.ImgType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static com.example.solidconnection.common.exception.ErrorCode.INVALID_NEWS_ACCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.never;

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
    private SiteUserFixture siteUserFixture;

    @Autowired
    private NewsFixture newsFixture;

    private SiteUser user;

    @BeforeEach
    void setUp() {
        user = siteUserFixture.멘토(1, "mentor");
    }

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
            NewsCommandResponse response = newsCommandService.createNews(user.getId(), request, imageFile);

            // then
            News savedNews = newsRepository.findById(response.id()).orElseThrow();
            assertThat(response.id()).isEqualTo(savedNews.getId());
        }
    }

    @Nested
    class 소식지_수정_테스트 {

        private final String CUSTOM_IMAGE_URL = "news/custom-image-url";
        private final String DEFAULT_IMAGE_URL = "news/default-logo.png";

        private News originNews;

        @Nested
        class 기본_필드_수정_테스트 {

            @BeforeEach
            void setUp() {
                originNews = newsFixture.소식지(user.getId());
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
                NewsUpdateRequest request = createNewsUpdateRequest(
                        expectedTitle,
                        expectedDescription,
                        expectedUrl,
                        null);

                // when
                NewsCommandResponse response = newsCommandService.updateNews(
                        user.getId(),
                        originNews.getId(),
                        request,
                        expectedFile);

                // then
                News savedNews = newsRepository.findById(response.id()).orElseThrow();
                assertAll(
                        () -> assertThat(savedNews.getTitle()).isEqualTo(expectedTitle),
                        () -> assertThat(savedNews.getDescription()).isEqualTo(expectedDescription),
                        () -> assertThat(savedNews.getThumbnailUrl()).isEqualTo(expectedNewImageUrl),
                        () -> assertThat(savedNews.getUrl()).isEqualTo(expectedUrl)
                );
            }

            @Test
            void 소식지_제목만_수정한다() {
                // given
                String expectedTitle = "제목 수정";
                String originalDescription = originNews.getDescription();
                String originalUrl = originNews.getUrl();
                String originalThumbnailUrl = originNews.getThumbnailUrl();
                NewsUpdateRequest request = createNewsUpdateRequest(
                        expectedTitle,
                        null,
                        null,
                        null);

                // when
                NewsCommandResponse response = newsCommandService.updateNews(
                        user.getId(),
                        originNews.getId(),
                        request,
                        null);

                // then
                News savedNews = newsRepository.findById(response.id()).orElseThrow();
                assertAll(
                        () -> assertThat(savedNews.getTitle()).isEqualTo(expectedTitle),
                        () -> assertThat(savedNews.getDescription()).isEqualTo(originalDescription),
                        () -> assertThat(savedNews.getUrl()).isEqualTo(originalUrl),
                        () -> assertThat(savedNews.getThumbnailUrl()).isEqualTo(originalThumbnailUrl)
                );
            }

            @Test
            void 공백으로_수정시_수정되지_않는다() {
                // given
                NewsUpdateRequest request = createNewsUpdateRequest("   ", "    ", null, null);

                // when
                NewsCommandResponse response = newsCommandService.updateNews(
                        user.getId(),
                        originNews.getId(),
                        request,
                        null);

                // then
                News savedNews = newsRepository.findById(response.id()).orElseThrow();
                assertAll(
                        () -> assertThat(savedNews.getTitle()).isEqualTo(originNews.getTitle()),
                        () -> assertThat(savedNews.getDescription()).isEqualTo(originNews.getDescription())
                );
            }

            @Test
            void 다른_사용자의_소식지를_수정하면_예외_응답을_반환한다() {
                // given
                SiteUser anotherUser = siteUserFixture.멘토(2, "anotherMentor");
                NewsUpdateRequest request = createNewsUpdateRequest(
                        "제목 수정",
                        null,
                        null,
                        null);

                // when & then
                assertThatCode(() -> newsCommandService.updateNews(
                        anotherUser.getId(),
                        originNews.getId(),
                        request,
                        null))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(INVALID_NEWS_ACCESS.getMessage());
            }
        }

        @Nested
        class 커스텀_이미지_관련_수정_테스트 {

            @BeforeEach
            void setUp() {
                originNews = newsFixture.소식지(user.getId(), CUSTOM_IMAGE_URL);
            }

            @Test
            void 기본_이미지로_변경_요청시_기존_커스텀_이미지를_삭제하고_기본_이미지로_변경한다() {
                // given
                NewsUpdateRequest request = createNewsUpdateRequest(
                        null,
                        null,
                        null,
                        true);

                // when
                NewsCommandResponse response = newsCommandService.updateNews(
                        user.getId(),
                        originNews.getId(),
                        request,
                        null);

                // then
                News savedNews = newsRepository.findById(response.id()).orElseThrow();
                assertThat(savedNews.getThumbnailUrl()).isEqualTo(DEFAULT_IMAGE_URL);
                then(s3Service).should().deletePostImage(CUSTOM_IMAGE_URL);
                then(s3Service).should(never()).uploadFile(null, ImgType.NEWS);
            }

            @Test
            void 새_이미지_업로드시_기존_커스텀_이미지를_삭제하고_새_이미지로_변경한다() {
                // given
                MultipartFile newImageFile = createImageFile();
                String newImageUrl = "news/new-image-url";
                given(s3Service.uploadFile(newImageFile, ImgType.NEWS))
                        .willReturn(new UploadedFileUrlResponse(newImageUrl));
                NewsUpdateRequest request = createNewsUpdateRequest(
                        null,
                        null,
                        null,
                        null);

                // when
                NewsCommandResponse response = newsCommandService.updateNews(
                        user.getId(),
                        originNews.getId(),
                        request,
                        newImageFile);

                // then
                News savedNews = newsRepository.findById(response.id()).orElseThrow();
                assertThat(savedNews.getThumbnailUrl()).isEqualTo(newImageUrl);
                then(s3Service).should().deletePostImage(CUSTOM_IMAGE_URL);
                then(s3Service).should().uploadFile(any(), any());
            }
        }

        @Nested
        class 기본_이미지_관련_수정_테스트 {

            @BeforeEach
            void setUp() {
                originNews = newsFixture.소식지(user.getId(), DEFAULT_IMAGE_URL);
            }

            @Test
            void 기본_이미지에서_기본_이미지로_변경_요청시_삭제_호출되지_않는다() {
                // given
                NewsUpdateRequest request = createNewsUpdateRequest(
                        null,
                        null,
                        null,
                        true);

                // when
                newsCommandService.updateNews(
                        user.getId(),
                        originNews.getId(),
                        request,
                        null);

                // then
                News savedNews = newsRepository.findById(originNews.getId()).orElseThrow();
                assertThat(savedNews.getThumbnailUrl()).isEqualTo(DEFAULT_IMAGE_URL);
                then(s3Service).should(never()).deletePostImage(DEFAULT_IMAGE_URL);
                then(s3Service).should(never()).uploadFile(any(), any());
            }

            @Test
            void 기본_이미지에서_새_이미지_업로드시_삭제_호출되지_않고_새_이미지로_변경한다() {
                // given
                MultipartFile newImageFile = createImageFile();
                String newImageUrl = "news/new-image-url";
                given(s3Service.uploadFile(newImageFile, ImgType.NEWS))
                        .willReturn(new UploadedFileUrlResponse(newImageUrl));
                NewsUpdateRequest request = createNewsUpdateRequest(null, null, null, null);

                // when
                newsCommandService.updateNews(
                        user.getId(),
                        originNews.getId(),
                        request,
                        newImageFile);

                // then
                News savedNews = newsRepository.findById(originNews.getId()).orElseThrow();
                assertThat(savedNews.getThumbnailUrl()).isEqualTo(newImageUrl);
                then(s3Service).should(never()).deletePostImage(DEFAULT_IMAGE_URL);
                then(s3Service).should().uploadFile(any(), any());
            }
        }
    }

    @Nested
    class 소식지_삭제_테스트 {

        @Test
        void 소식지를_성공적으로_삭제한다() {
            // given
            News originNews = newsFixture.소식지(user.getId());
            String expectedImageUrl = originNews.getThumbnailUrl();

            // when
            NewsCommandResponse response = newsCommandService.deleteNewsById(user, originNews.getId());

            // then
            assertThat(response.id()).isEqualTo(originNews.getId());
            assertThat(newsRepository.findById(originNews.getId())).isEmpty();
            then(s3Service).should().deletePostImage(expectedImageUrl);
        }
    }

    private NewsCreateRequest createNewsCreateRequest() {
        return new NewsCreateRequest("제목", "설명", "https://youtu.be/test");
    }

    private NewsUpdateRequest createNewsUpdateRequest(String title, String description, String url, Boolean resetToDefaultImage) {
        return new NewsUpdateRequest(title, description, url, resetToDefaultImage);
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
