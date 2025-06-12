package com.example.solidconnection.news.service;

import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.NewsCreateRequest;
import com.example.solidconnection.news.dto.NewsResponse;
import com.example.solidconnection.news.repository.NewsRepository;
import com.example.solidconnection.s3.domain.ImgType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
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
