package com.example.solidconnection.s3.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.s3.domain.UploadPath;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@DisplayName("S3 서비스 테스트")
@ExtendWith(MockitoExtension.class)
public class S3ServiceTest {

    @InjectMocks
    private S3Service s3Service;

    private static final long MAX_FILE_SIZE_MB = 1024 * 1024 * 5;

    private MockMultipartFile createMockFile(String originalName, long size) {
        return new MockMultipartFile("file", originalName, "image/jpeg", new byte[(int) size]);
    }

    @Nested
    class 파일_업로드_경로_및_리사이징_로직 {

        @Test
        void O5MB_미만의_이미지는_원본_확장자를_유지하며_업로드된다() {
            // given
            MockMultipartFile file = createMockFile("test.png", MAX_FILE_SIZE_MB - 100);

            // when
            UploadedFileUrlResponse response = s3Service.uploadFile(file, UploadPath.PROFILE);

            // then
            assertAll(
                    () -> assertThat(response.fileUrl()).startsWith("profile/"),
                    () -> assertThat(response.fileUrl()).endsWith(".png"),
                    () -> assertThat(response.fileUrl()).doesNotContain("original/", "resize/")
            );
        }

        @Test
        void O5MB_이상의_이미지는_original_경로로_업로드되고_resize_webp_경로를_반환한다() {
            // given
            MockMultipartFile file = createMockFile("test.jpg", MAX_FILE_SIZE_MB + 100);

            // when
            UploadedFileUrlResponse response = s3Service.uploadFile(file, UploadPath.PROFILE);

            // then
            assertAll(
                    () -> assertThat(response.fileUrl()).startsWith("resize/profile/"),
                    () -> assertThat(response.fileUrl()).endsWith(".webp")
            );
        }

        @Test
        void 채팅_파일은_5MB가_넘어도_리사이징_경로를_적용하지_않고_원본_경로를_반환한다() {
            // given
            MockMultipartFile file = createMockFile("chat.jpg", MAX_FILE_SIZE_MB + 100);

            // when
            UploadedFileUrlResponse response = s3Service.uploadFile(file, UploadPath.CHAT);

            // then
            assertAll(
                    () -> assertThat(response.fileUrl()).startsWith("chat/files/"),
                    () -> assertThat(response.fileUrl()).endsWith(".jpg"),
                    () -> assertThat(response.fileUrl()).doesNotContain("resize/")
            );
        }
    }

    @Nested
    class 파일_검증 {

        @Test
        void 허용되지_않은_확장자의_파일은_예외를_던진다() {
            // given
            MockMultipartFile invalidFile = createMockFile("virus.exe", 100);

            // when & then
            assertThatThrownBy(() -> s3Service.uploadFile(invalidFile, UploadPath.PROFILE))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("허용된 형식");
        }

        @Test
        void 채팅_업로드시_이미지_외의_허용된_문서_확장자들도_성공적으로_검증을_통과한다() {
            // given
            MockMultipartFile pdfFile = createMockFile("test.pdf", 100);
            MockMultipartFile wordFile = createMockFile("test.docx", 100);

            // when & then
            assertAll(
                    () -> assertThatCode(() -> s3Service.uploadFile(pdfFile, UploadPath.CHAT))
                            .doesNotThrowAnyException(),
                    () -> assertThatCode(() -> s3Service.uploadFile(wordFile, UploadPath.CHAT))
                            .doesNotThrowAnyException()
            );
        }
    }
}
