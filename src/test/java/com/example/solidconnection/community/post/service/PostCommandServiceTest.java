package com.example.solidconnection.community.post.service;

import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostImage;
import com.example.solidconnection.community.post.dto.PostCreateRequest;
import com.example.solidconnection.community.post.dto.PostCreateResponse;
import com.example.solidconnection.community.post.dto.PostDeleteResponse;
import com.example.solidconnection.community.post.dto.PostUpdateRequest;
import com.example.solidconnection.community.post.dto.PostUpdateResponse;
import com.example.solidconnection.community.post.repository.PostImageRepository;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.s3.S3Service;
import com.example.solidconnection.s3.UploadedFileUrlResponse;
import com.example.solidconnection.service.RedisService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.type.ImgType;
import com.example.solidconnection.type.PostCategory;
import com.example.solidconnection.util.RedisUtils;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.CAN_NOT_DELETE_OR_UPDATE_QUESTION;
import static com.example.solidconnection.custom.exception.ErrorCode.CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_ACCESS;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_CATEGORY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("게시글 생성/수정/삭제 서비스 테스트")
class PostCommandServiceTest extends BaseIntegrationTest {

    @Autowired
    private PostCommandService postCommandService;

    @MockBean
    private S3Service s3Service;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Nested
    class 게시글_생성_테스트 {

        @Test
        @Transactional
        void 게시글을_성공적으로_생성한다() {
            // given
            PostCreateRequest request = createPostCreateRequest(PostCategory.자유.name());
            List<MultipartFile> imageFiles = List.of(createImageFile());
            String expectedImageUrl = "test-image-url";
            given(s3Service.uploadFiles(any(), eq(ImgType.COMMUNITY)))
                    .willReturn(List.of(new UploadedFileUrlResponse(expectedImageUrl)));

            // when
            PostCreateResponse response = postCommandService.createPost(
                    테스트유저_1,
                    request,
                    imageFiles
            );

            // then
            Post savedPost = postRepository.findById(response.id()).orElseThrow();
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(savedPost.getId()),
                    () -> assertThat(savedPost.getTitle()).isEqualTo(request.title()),
                    () -> assertThat(savedPost.getContent()).isEqualTo(request.content()),
                    () -> assertThat(savedPost.getIsQuestion()).isEqualTo(request.isQuestion()),
                    () -> assertThat(savedPost.getCategory().name()).isEqualTo(request.postCategory()),
                    () -> assertThat(savedPost.getBoard().getCode()).isEqualTo(자유게시판.getCode()),
                    () -> assertThat(savedPost.getPostImageList()).hasSize(imageFiles.size()),
                    () -> assertThat(savedPost.getPostImageList())
                            .extracting(PostImage::getUrl)
                            .containsExactly(expectedImageUrl)
            );
        }

        @Test
        void 전체_카테고리로_생성하면_예외_응답을_반환한다() {
            // given
            PostCreateRequest request = createPostCreateRequest(PostCategory.전체.name());
            List<MultipartFile> imageFiles = List.of();

            // when & then
            assertThatThrownBy(() ->
                    postCommandService.createPost(테스트유저_1, request, imageFiles))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_CATEGORY.getMessage());
        }

        @Test
        void 존재하지_않는_카테고리로_생성하면_예외_응답을_반환한다() {
            // given
            PostCreateRequest request = createPostCreateRequest("INVALID_CATEGORY");
            List<MultipartFile> imageFiles = List.of();

            // when & then
            assertThatThrownBy(() ->
                    postCommandService.createPost(테스트유저_1, request, imageFiles))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_CATEGORY.getMessage());
        }

        @Test
        void 이미지를_5개_초과하여_업로드하면_예외_응답을_반환한다() {
            // given
            PostCreateRequest request = createPostCreateRequest(PostCategory.자유.name());
            List<MultipartFile> imageFiles = createSixImageFiles();

            // when & then
            assertThatThrownBy(() ->
                    postCommandService.createPost(테스트유저_1, request, imageFiles))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES.getMessage());
        }
    }

    @Nested
    class 게시글_수정_테스트 {

        @Test
        @Transactional
        void 게시글을_성공적으로_수정한다() {
            // given
            String originImageUrl = "origin-image-url";
            String expectedImageUrl = "update-image-url";
            Post testPost = createPost(자유게시판, 테스트유저_1, originImageUrl);
            PostUpdateRequest request = createPostUpdateRequest();
            List<MultipartFile> imageFiles = List.of(createImageFile());

            given(s3Service.uploadFiles(any(), eq(ImgType.COMMUNITY)))
                    .willReturn(List.of(new UploadedFileUrlResponse(expectedImageUrl)));

            // when
            PostUpdateResponse response = postCommandService.updatePost(
                    테스트유저_1,
                    testPost.getId(),
                    request,
                    imageFiles
            );

            // then
            Post updatedPost = postRepository.findById(response.id()).orElseThrow();
            assertAll(
                    () -> assertThat(updatedPost.getTitle()).isEqualTo(request.title()),
                    () -> assertThat(updatedPost.getContent()).isEqualTo(request.content()),
                    () -> assertThat(updatedPost.getCategory().name()).isEqualTo(request.postCategory()),
                    () -> assertThat(updatedPost.getPostImageList()).hasSize(imageFiles.size()),
                    () -> assertThat(updatedPost.getPostImageList())
                            .extracting(PostImage::getUrl)
                            .containsExactly(expectedImageUrl)
            );
            then(s3Service).should().deletePostImage(originImageUrl);
        }

        @Test
        void 다른_사용자의_게시글을_수정하면_예외_응답을_반환한다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1, "origin-image-url");
            PostUpdateRequest request = createPostUpdateRequest();
            List<MultipartFile> imageFiles = List.of();

            // when & then
            assertThatThrownBy(() ->
                    postCommandService.updatePost(
                            테스트유저_2,
                            testPost.getId(),
                            request,
                            imageFiles
                    ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_ACCESS.getMessage());
        }

        @Test
        void 질문_게시글을_수정하면_예외_응답을_반환한다() {
            // given
            Post testPost = createQuestionPost(자유게시판, 테스트유저_1, "origin-image-url");
            PostUpdateRequest request = createPostUpdateRequest();
            List<MultipartFile> imageFiles = List.of();

            // when & then
            assertThatThrownBy(() ->
                    postCommandService.updatePost(
                            테스트유저_1,
                            testPost.getId(),
                            request,
                            imageFiles
                    ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(CAN_NOT_DELETE_OR_UPDATE_QUESTION.getMessage());
        }

        @Test
        void 이미지를_5개_초과하여_수정하면_예외_응답을_반환한다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1, "origin-image-url");
            PostUpdateRequest request = createPostUpdateRequest();
            List<MultipartFile> imageFiles = createSixImageFiles();

            // when & then
            assertThatThrownBy(() ->
                    postCommandService.updatePost(
                            테스트유저_1,
                            testPost.getId(),
                            request,
                            imageFiles
                    ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES.getMessage());
        }
    }

    @Nested
    class 게시글_삭제_테스트 {

        @Test
        void 게시글을_성공적으로_삭제한다() {
            // given
            String originImageUrl = "origin-image-url";
            Post testPost = createPost(자유게시판, 테스트유저_1, originImageUrl);
            String viewCountKey = redisUtils.getPostViewCountRedisKey(testPost.getId());
            redisService.increaseViewCount(viewCountKey);

            // when
            PostDeleteResponse response = postCommandService.deletePostById(
                    테스트유저_1,
                    testPost.getId()
            );

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(testPost.getId()),
                    () -> assertThat(postRepository.findById(testPost.getId())).isEmpty(),
                    () -> assertThat(redisService.isKeyExists(viewCountKey)).isFalse()
            );
            then(s3Service).should().deletePostImage(originImageUrl);
        }

        @Test
        void 다른_사용자의_게시글을_삭제하면_예외_응답을_반환한다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1, "origin-image-url");

            // when & then
            assertThatThrownBy(() ->
                    postCommandService.deletePostById(
                            테스트유저_2,
                            testPost.getId()
                    ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_ACCESS.getMessage());
        }

        @Test
        void 질문_게시글을_삭제하면_예외_응답을_반환한다() {
            // given
            Post testPost = createQuestionPost(자유게시판, 테스트유저_1, "origin-image-url");

            // when & then
            assertThatThrownBy(() ->
                    postCommandService.deletePostById(
                            테스트유저_1,
                            testPost.getId()
                    ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(CAN_NOT_DELETE_OR_UPDATE_QUESTION.getMessage());
        }
    }

    private PostCreateRequest createPostCreateRequest(String category) {
        return new PostCreateRequest(
                자유게시판.getCode(),
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

    private Post createPost(Board board, SiteUser siteUser, String originImageUrl) {
        Post post = new Post(
                "원본 제목",
                "원본 내용",
                false,
                0L,
                0L,
                PostCategory.자유
        );
        post.setBoardAndSiteUser(board, siteUser);
        Post savedPost = postRepository.save(post);
        PostImage postImage = new PostImage(originImageUrl);
        postImage.setPost(savedPost);
        postImageRepository.save(postImage);
        return savedPost;
    }

    private Post createQuestionPost(Board board, SiteUser siteUser, String originImageUrl) {
        Post post = new Post(
                "질문 제목",
                "질문 내용",
                true,
                0L,
                0L,
                PostCategory.질문
        );
        post.setBoardAndSiteUser(board, siteUser);
        Post savedPost = postRepository.save(post);
        PostImage postImage = new PostImage(originImageUrl);
        postImage.setPost(savedPost);
        postImageRepository.save(postImage);
        return savedPost;
    }

    private PostUpdateRequest createPostUpdateRequest() {
        return new PostUpdateRequest(
                PostCategory.자유.name(),
                "수정된 제목",
                "수정된 내용"
        );
    }
}
