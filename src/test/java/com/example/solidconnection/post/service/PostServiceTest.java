package com.example.solidconnection.post.service;

import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.board.repository.BoardRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.PostImage;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.dto.PostCreateRequest;
import com.example.solidconnection.post.dto.PostCreateResponse;
import com.example.solidconnection.post.repository.PostRepository;
import com.example.solidconnection.s3.S3Service;
import com.example.solidconnection.s3.UploadedFileUrlResponse;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.type.BoardCode;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.ImgType;
import com.example.solidconnection.type.PostCategory;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_BOARD_CODE;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_CATEGORY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;

@DisplayName("게시글 서비스 테스트")
class PostServiceTest extends BaseIntegrationTest {

    @Autowired
    private PostService postService;

    @MockBean
    private S3Service s3Service;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PostRepository postRepository;

    @Nested
    class 게시글_생성_테스트 {

        @Test
        @Transactional
        void 게시글을_성공적으로_생성한다() {
            // given
            SiteUser testUser = createSiteUser();
            PostCreateRequest request = createPostCreateRequest(PostCategory.자유.name());
            List<MultipartFile> imageFiles = List.of(createImageFile());
            Board testBoard = createBoard(BoardCode.FREE);
            String expectedImageUrl = "test-image-url";
            given(s3Service.uploadFiles(any(), eq(ImgType.COMMUNITY)))
                    .willReturn(List.of(new UploadedFileUrlResponse(expectedImageUrl)));

            // when
            PostCreateResponse response = postService.createPost(
                    testUser.getEmail(),
                    testBoard.getCode(),
                    request,
                    imageFiles
            );
            Post savedPost = postRepository.findById(response.id()).orElseThrow();

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(savedPost.getId()),
                    () -> assertThat(savedPost.getTitle()).isEqualTo(request.title()),
                    () -> assertThat(savedPost.getContent()).isEqualTo(request.content()),
                    () -> assertThat(savedPost.getIsQuestion()).isEqualTo(request.isQuestion()),
                    () -> assertThat(savedPost.getCategory().name()).isEqualTo(request.postCategory()),
                    () -> assertThat(savedPost.getBoard().getCode()).isEqualTo(testBoard.getCode()),
                    () -> assertThat(savedPost.getPostImageList()).hasSize(imageFiles.size()),
                    () -> assertThat(savedPost.getPostImageList())
                            .extracting(PostImage::getUrl)
                            .containsExactly(expectedImageUrl)
            );
        }

        @Test
        void 유효하지_않은_게시판_코드로_생성하면_예외_응답을_반환한다() {
            // given
            SiteUser testUser = createSiteUser();
            PostCreateRequest request = createPostCreateRequest(PostCategory.자유.name());
            List<MultipartFile> imageFiles = List.of();

            // when & then
            assertThatThrownBy(() ->
                    postService.createPost(testUser.getEmail(), "INVALID_CODE", request, imageFiles))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_BOARD_CODE.getMessage());
        }

        @Test
        void 전체_카테고리로_생성하면_예외_응답을_반환한다() {
            // given
            SiteUser testUser = createSiteUser();
            PostCreateRequest request = createPostCreateRequest(PostCategory.전체.name());
            List<MultipartFile> imageFiles = List.of();
            Board testBoard = createBoard(BoardCode.FREE);

            // when & then
            assertThatThrownBy(() ->
                    postService.createPost(testUser.getEmail(), testBoard.getCode(), request, imageFiles))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_CATEGORY.getMessage());
        }

        @Test
        void 존재하지_않는_카테고리로_생성하면_예외_응답을_반환한다() {
            // given
            SiteUser testUser = createSiteUser();
            PostCreateRequest request = createPostCreateRequest("INVALID_CATEGORY");
            List<MultipartFile> imageFiles = List.of();
            Board testBoard = createBoard(BoardCode.FREE);

            // when & then
            assertThatThrownBy(() ->
                    postService.createPost(testUser.getEmail(), testBoard.getCode(), request, imageFiles))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_CATEGORY.getMessage());
        }

        @Test
        void 이미지를_5개_초과하여_업로드하면_예외_응답을_반환한다() {
            // given
            SiteUser testUser = createSiteUser();
            PostCreateRequest request = createPostCreateRequest(PostCategory.자유.name());
            List<MultipartFile> imageFiles = createSixImageFiles();
            Board testBoard = createBoard(BoardCode.FREE);

            // when & then
            assertThatThrownBy(() ->
                    postService.createPost(testUser.getEmail(), testBoard.getCode(), request, imageFiles))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES.getMessage());
        }
    }

    private SiteUser createSiteUser() {
        SiteUser siteUser = new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
        return siteUserRepository.save(siteUser);
    }

    private Board createBoard(BoardCode boardCode) {
        Board board = new Board(boardCode.name(), "자유게시판");
        return boardRepository.save(board);
    }

    private PostCreateRequest createPostCreateRequest(String category) {
        return new PostCreateRequest(
                category,
                "테스트 제목",
                "테스트 내용",
                false
        );
    }

    private MockMultipartFile createImageFile() {
        return new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    private List<MultipartFile> createSixImageFiles() {
        return List.of(
                createImageFile(),
                createImageFile(),
                createImageFile(),
                createImageFile(),
                createImageFile(),
                createImageFile()
        );
    }
}
