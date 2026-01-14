package com.example.solidconnection.community.post.service;

import static com.example.solidconnection.common.exception.ErrorCode.CAN_NOT_DELETE_OR_UPDATE_QUESTION;
import static com.example.solidconnection.common.exception.ErrorCode.CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_POST_ACCESS;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_POST_CATEGORY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.community.board.fixture.BoardFixture;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostCategory;
import com.example.solidconnection.community.post.domain.PostImage;
import com.example.solidconnection.community.post.dto.PostCreateRequest;
import com.example.solidconnection.community.post.dto.PostCreateResponse;
import com.example.solidconnection.community.post.dto.PostDeleteResponse;
import com.example.solidconnection.community.post.dto.PostUpdateRequest;
import com.example.solidconnection.community.post.dto.PostUpdateResponse;
import com.example.solidconnection.community.post.fixture.PostFixture;
import com.example.solidconnection.community.post.fixture.PostImageFixture;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.s3.domain.UploadType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.util.RedisUtils;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@TestContainerSpringBootTest
@DisplayName("게시글 생성/수정/삭제 서비스 테스트")
class PostCommandServiceTest {

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
    private SiteUserFixture siteUserFixture;

    @Autowired
    private BoardFixture boardFixture;

    @Autowired
    private PostFixture postFixture;

    @Autowired
    private PostImageFixture postImageFixture;

    private SiteUser user1;
    private Post post;
    private Post questionPost;

    @BeforeEach
    void setUp() {
        user1 = siteUserFixture.사용자(1, "test1");
        post = postFixture.게시글(
                "제목",
                "내용",
                false,
                PostCategory.자유,
                boardFixture.자유게시판(),
                user1
        );
        questionPost = postFixture.게시글(
                "제목",
                "내용",
                true,
                PostCategory.질문,
                boardFixture.자유게시판(),
                user1
        );
    }

    @Nested
    class 게시글_생성_테스트 {

        @Test
        @Transactional
        void 게시글을_성공적으로_생성한다() {
            // given
            PostCreateRequest request = createPostCreateRequest(PostCategory.자유.name());
            List<MultipartFile> imageFiles = List.of(createImageFile());
            String expectedImageUrl = "test-image-url";
            given(s3Service.uploadFiles(any(), eq(UploadType.COMMUNITY)))
                    .willReturn(List.of(new UploadedFileUrlResponse(expectedImageUrl)));

            // when
            PostCreateResponse response = postCommandService.createPost(user1.getId(), request, imageFiles);

            // then
            Post savedPost = postRepository.findById(response.id()).orElseThrow();
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(savedPost.getId()),
                    () -> assertThat(savedPost.getPostImageList()).hasSize(imageFiles.size()),
                    () -> assertThat(savedPost.getPostImageList())
                            .extracting(PostImage::getUrl)
                            .containsExactly(expectedImageUrl)
            );
        }

        @Test
        void 전체_카테고리로_생성하면_예외가_발생한다() {
            // given
            PostCreateRequest request = createPostCreateRequest(PostCategory.전체.name());
            List<MultipartFile> imageFiles = List.of();

            // when & then
            assertThatThrownBy(() ->
                                       postCommandService.createPost(user1.getId(), request, imageFiles))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_CATEGORY.getMessage());
        }

        @Test
        void 존재하지_않는_카테고리로_생성하면_예외가_발생한다() {
            // given
            PostCreateRequest request = createPostCreateRequest("INVALID_CATEGORY");
            List<MultipartFile> imageFiles = List.of();

            // when & then
            assertThatThrownBy(() ->
                                       postCommandService.createPost(user1.getId(), request, imageFiles))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_CATEGORY.getMessage());
        }

        @Test
        void 이미지를_5개_초과하여_업로드하면_예외가_발생한다() {
            // given
            PostCreateRequest request = createPostCreateRequest(PostCategory.자유.name());
            List<MultipartFile> imageFiles = createSixImageFiles();

            // when & then
            assertThatThrownBy(() ->
                                       postCommandService.createPost(user1.getId(), request, imageFiles))
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
            postImageFixture.게시글_이미지(originImageUrl, post);
            String expectedImageUrl = "update-image-url";
            PostUpdateRequest request = createPostUpdateRequest();
            List<MultipartFile> imageFiles = List.of(createImageFile());

            given(s3Service.uploadFiles(any(), eq(UploadType.COMMUNITY)))
                    .willReturn(List.of(new UploadedFileUrlResponse(expectedImageUrl)));

            // when
            PostUpdateResponse response = postCommandService.updatePost(
                    user1.getId(),
                    post.getId(),
                    request,
                    imageFiles
            );

            // then
            Post updatedPost = postRepository.findById(response.id()).orElseThrow();
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(updatedPost.getId()),
                    () -> assertThat(updatedPost.getPostImageList()).hasSize(imageFiles.size()),
                    () -> assertThat(updatedPost.getPostImageList())
                            .extracting(PostImage::getUrl)
                            .containsExactly(expectedImageUrl)
            );
            then(s3Service).should().deletePostImage(originImageUrl);
        }

        @Test
        void 다른_사용자의_게시글을_수정하면_예외가_발생한다() {
            // given
            SiteUser user2 = siteUserFixture.사용자(2, "test2");
            PostUpdateRequest request = createPostUpdateRequest();
            List<MultipartFile> imageFiles = List.of();

            // when & then
            assertThatThrownBy(() ->
                                       postCommandService.updatePost(
                                               user2.getId(),
                                               post.getId(),
                                               request,
                                               imageFiles
                                       ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_ACCESS.getMessage());
        }

        @Test
        void 질문_게시글을_수정하면_예외가_발생한다() {
            // given
            PostUpdateRequest request = createPostUpdateRequest();
            List<MultipartFile> imageFiles = List.of();

            // when & then
            assertThatThrownBy(() ->
                                       postCommandService.updatePost(
                                               user1.getId(),
                                               questionPost.getId(),
                                               request,
                                               imageFiles
                                       ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(CAN_NOT_DELETE_OR_UPDATE_QUESTION.getMessage());
        }

        @Test
        void 이미지를_5개_초과하여_수정하면_예외가_발생한다() {
            // given
            PostUpdateRequest request = createPostUpdateRequest();
            List<MultipartFile> imageFiles = createSixImageFiles();

            // when & then
            assertThatThrownBy(() ->
                                       postCommandService.updatePost(
                                               user1.getId(),
                                               post.getId(),
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
            postImageFixture.게시글_이미지(originImageUrl, post);
            String viewCountKey = redisUtils.getPostViewCountRedisKey(post.getId());
            redisService.increaseViewCount(viewCountKey);

            // when
            PostDeleteResponse response = postCommandService.deletePostById(user1.getId(), post.getId());

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(post.getId()),
                    () -> assertThat(postRepository.findById(post.getId())).isEmpty(),
                    () -> assertThat(redisService.isKeyExists(viewCountKey)).isFalse()
            );
            then(s3Service).should().deletePostImage(originImageUrl);
        }

        @Test
        void 다른_사용자의_게시글을_삭제하면_예외가_발생한다() {
            // given
            SiteUser user2 = siteUserFixture.사용자(2, "test2");

            // when & then
            assertThatThrownBy(() ->
                                       postCommandService.deletePostById(
                                               user2.getId(),
                                               post.getId()
                                       ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_ACCESS.getMessage());
        }

        @Test
        void 질문_게시글을_삭제하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() ->
                                       postCommandService.deletePostById(
                                               user1.getId(),
                                               questionPost.getId()
                                       ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(CAN_NOT_DELETE_OR_UPDATE_QUESTION.getMessage());
        }
    }

    private PostCreateRequest createPostCreateRequest(String category) {
        return new PostCreateRequest(
                boardFixture.자유게시판().getCode(),
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

    private PostUpdateRequest createPostUpdateRequest() {
        return new PostUpdateRequest(
                PostCategory.자유.name(),
                "수정된 제목",
                "수정된 내용"
        );
    }
}
