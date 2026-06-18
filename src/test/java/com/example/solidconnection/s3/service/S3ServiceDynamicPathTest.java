package com.example.solidconnection.s3.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.s3.domain.UploadPath;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

@DisplayName("S3 서비스 동적 업로드 경로 테스트")
@ExtendWith(MockitoExtension.class)
class S3ServiceDynamicPathTest {

    @InjectMocks
    private S3Service s3Service;

    @Mock
    private S3Client s3Client;

    @Mock
    private SiteUserRepository siteUserRepository;

    @Mock
    private FileUploadService fileUploadService;

    @Nested
    class 동적_하위_디렉토리_업로드_테스트 {

        @Test
        void 업로드_경로와_파일명_사이에_동적_하위_디렉토리를_포함한다() {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", new byte[100]);

            // when
            UploadedFileUrlResponse response = s3Service.uploadFile(
                    file,
                    UploadPath.ADMIN_UNIVERSITY_LOGO,
                    "university_of_tokyo"
            );

            // then
            assertAll(
                    () -> assertThat(response.fileUrl()).startsWith("admin/logo/university_of_tokyo/"),
                    () -> assertThat(response.fileUrl()).endsWith(".png")
            );
        }
    }
}
