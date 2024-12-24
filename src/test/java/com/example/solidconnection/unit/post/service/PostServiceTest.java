package com.example.solidconnection.unit.post.service;

import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.board.dto.PostFindBoardResponse;
import com.example.solidconnection.board.repository.BoardRepository;
import com.example.solidconnection.comment.dto.PostFindCommentResponse;
import com.example.solidconnection.comment.service.CommentService;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.post.dto.PostFindPostImageResponse;
import com.example.solidconnection.entity.PostImage;
import com.example.solidconnection.post.domain.PostLike;
import com.example.solidconnection.post.repository.PostLikeRepository;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.dto.*;
import com.example.solidconnection.post.repository.PostRepository;
import com.example.solidconnection.post.service.PostService;
import com.example.solidconnection.s3.S3Service;
import com.example.solidconnection.s3.UploadedFileUrlResponse;
import com.example.solidconnection.service.RedisService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.PostFindSiteUserResponse;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.*;
import com.example.solidconnection.util.RedisUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static com.example.solidconnection.custom.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@DisplayName("게시글 서비스 테스트")
class PostServiceTest {
    @InjectMocks
    PostService postService;
    @Mock
    PostRepository postRepository;
    @Mock
    SiteUserRepository siteUserRepository;
    @Mock
    BoardRepository boardRepository;
    @Mock
    PostLikeRepository postLikeRepository;
    @Mock
    S3Service s3Service;
    @Mock
    CommentService commentService;
    @Mock
    RedisService redisService;
    @Mock
    RedisUtils redisUtils;

    private SiteUser siteUser;
    private Board board;
    private Post post;
    private Post postWithImages;
    private Post questionPost;
    private PostLike postLike;
    private List<MultipartFile> imageFiles;
    private List<MultipartFile> imageFilesWithMoreThanFiveFiles;
    private List<UploadedFileUrlResponse> uploadedFileUrlResponseList;


    @BeforeEach
    void setUp() {
        siteUser = createSiteUser();
        board = createBoard();
        imageFiles = createMockImageFiles();
        imageFilesWithMoreThanFiveFiles = createMockImageFilesWithMoreThanFiveFiles();
        uploadedFileUrlResponseList = createUploadedFileUrlResponses();
        post = createPost(board, siteUser);
        postWithImages = createPostWithImages(board, siteUser);
        questionPost = createQuestionPost(board, siteUser);
        postLike = createPostLike(post, siteUser);
    }

    private SiteUser createSiteUser() {
        return new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
    }

    private Board createBoard() {
        return new Board(
                "FREE", "자유게시판");
    }

    private Post createPost(Board board, SiteUser siteUser) {
        Post post = new Post(
                "title",
                "content",
                false,
                0L,
                0L,
                PostCategory.valueOf("자유")
        );
        post.setBoardAndSiteUser(board, siteUser);

        return post;
    }

    private Post createPostWithImages(Board board, SiteUser siteUser) {
        Post postWithImages = new Post(
                "title",
                "content",
                false,
                0L,
                0L,
                PostCategory.valueOf("자유")
        );
        postWithImages.setBoardAndSiteUser(board, siteUser);

        List<PostImage> postImageList = new ArrayList<>();
        postImageList.add(new PostImage("https://s3.example.com/test1.png"));
        postImageList.add(new PostImage("https://s3.example.com/test2.png"));
        for (PostImage postImage : postImageList) {
            postImage.setPost(postWithImages);
        }
        return postWithImages;
    }

    private Post createQuestionPost(Board board, SiteUser siteUser) {
        Post post = new Post(
                "title",
                "content",
                true,
                0L,
                0L,
                PostCategory.valueOf("자유")
        );
        post.setBoardAndSiteUser(board, siteUser);
        return post;
    }

    private PostLike createPostLike(Post post, SiteUser siteUser) {
        PostLike postLike = new PostLike();
        postLike.setPostAndSiteUser(post, siteUser);
        return postLike;
    }

    private List<MultipartFile> createMockImageFiles() {
        List<MultipartFile> multipartFileList = new ArrayList<>();
        multipartFileList.add(new MockMultipartFile("file1", "test1.png",
                "image/png", "test image content 1".getBytes()));
        multipartFileList.add(new MockMultipartFile("file2", "test1.png",
                "image/png", "test image content 1".getBytes()));
        return multipartFileList;
    }

    private List<UploadedFileUrlResponse> createUploadedFileUrlResponses() {
        return Arrays.asList(
                new UploadedFileUrlResponse("https://s3.example.com/test1.png"),
                new UploadedFileUrlResponse("https://s3.example.com/test2.png")
        );
    }

    private List<MultipartFile> createMockImageFilesWithMoreThanFiveFiles() {
        List<MultipartFile> multipartFileList = new ArrayList<>();
        multipartFileList.add(new MockMultipartFile("file1", "test1.png",
                "image/png", "test image content 1".getBytes()));
        multipartFileList.add(new MockMultipartFile("file2", "test1.png",
                "image/png", "test image content 1".getBytes()));
        multipartFileList.add(new MockMultipartFile("file3", "test1.png",
                "image/png", "test image content 1".getBytes()));
        multipartFileList.add(new MockMultipartFile("file4", "test1.png",
                "image/png", "test image content 1".getBytes()));
        multipartFileList.add(new MockMultipartFile("file5", "test1.png",
                "image/png", "test image content 1".getBytes()));
        multipartFileList.add(new MockMultipartFile("file6", "test1.png",
                "image/png", "test image content 1".getBytes()));
        return multipartFileList;
    }

    /**
     * 게시글 등록
     */
    @Test
    void 게시글을_등록한다_이미지_있음() {
        // Given
        PostCreateRequest postCreateRequest = new PostCreateRequest(
                "자유", "title", "content", false);
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(boardRepository.getByCode(board.getCode())).thenReturn(board);
        when(s3Service.uploadFiles(imageFiles, ImgType.COMMUNITY)).thenReturn(uploadedFileUrlResponseList);
        when(postRepository.save(any(Post.class))).thenReturn(postWithImages);

        // When
        PostCreateResponse postCreateResponse = postService.createPost(
                siteUser.getEmail(), board.getCode(), postCreateRequest, imageFiles);

        // Then
        assertEquals(postCreateResponse, PostCreateResponse.from(postWithImages));
        verify(siteUserRepository, times(1)).getByEmail(siteUser.getEmail());
        verify(boardRepository, times(1)).getByCode(board.getCode());
        verify(s3Service, times(1)).uploadFiles(imageFiles, ImgType.COMMUNITY);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void 게시글을_등록한다_이미지_없음() {
        // Given
        PostCreateRequest postCreateRequest = new PostCreateRequest(
                "자유", "title", "content", false);
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(boardRepository.getByCode(board.getCode())).thenReturn(board);
        when(postRepository.save(postCreateRequest.toEntity(siteUser, board))).thenReturn(post);

        // When
        PostCreateResponse postCreateResponse = postService.createPost(
                siteUser.getEmail(), board.getCode(), postCreateRequest, Collections.emptyList());

        // Then
        assertEquals(postCreateResponse, PostCreateResponse.from(post));
        verify(siteUserRepository, times(1)).getByEmail(siteUser.getEmail());
        verify(boardRepository, times(1)).getByCode(board.getCode());
        verify(postRepository, times(1)).save(postCreateRequest.toEntity(siteUser, board));
    }

    @Test
    void 게시글을_등록할_때_유효한_게시판이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidBoardCode = "INVALID_CODE";
        PostCreateRequest postCreateRequest = new PostCreateRequest(
                "자유", "title", "content", false);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> postService
                .createPost(siteUser.getEmail(), invalidBoardCode, postCreateRequest, Collections.emptyList()));
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_BOARD_CODE.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_BOARD_CODE.getCode());
    }

    @Test
    void 게시글을_등록할_때_유효한_카테고리가_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidPostCategory = "invalidPostCategory";
        PostCreateRequest postCreateRequest = new PostCreateRequest(
                invalidPostCategory, "title", "content", false);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> postService
                .createPost(siteUser.getEmail(), board.getCode(), postCreateRequest, Collections.emptyList()));
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_POST_CATEGORY.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_POST_CATEGORY.getCode());
    }

    @Test
    void 게시글을_등록할_때_파일_수가_5개를_넘는다면_예외_응답을_반환한다() {
        // Given
        PostCreateRequest postCreateRequest = new PostCreateRequest(
                "자유", "title", "content", false);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> postService
                .createPost(siteUser.getEmail(), board.getCode(), postCreateRequest, imageFilesWithMoreThanFiveFiles));
        assertThat(exception.getMessage())
                .isEqualTo(CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES.getCode());
    }

    /**
     * 게시글 수정
     */
    @Test
    void 게시글을_수정한다_기존_사진_없음_수정_사진_없음() {
        // Given
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("질문", "updateTitle", "updateContent");
        when(postRepository.getById(post.getId())).thenReturn(post);

        // When
        PostUpdateResponse response = postService.updatePost(
                siteUser.getEmail(), board.getCode(), post.getId(), postUpdateRequest, Collections.emptyList());

        // Then
        assertEquals(response, PostUpdateResponse.from(post));
        verify(postRepository, times(1)).getById(post.getId());
        verify(s3Service, times(0)).deletePostImage(any(String.class));
        verify(s3Service, times(0)).uploadFiles(anyList(), any(ImgType.class));
    }

    @Test
    void 게시글을_수정한다_기존_사진_있음_수정_사진_없음() {
        // Given
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("자유", "updateTitle", "updateContent");
        when(postRepository.getById(postWithImages.getId())).thenReturn(postWithImages);

        // When
        PostUpdateResponse response = postService.updatePost(
                siteUser.getEmail(), board.getCode(), postWithImages.getId(), postUpdateRequest, Collections.emptyList());

        // Then
        assertEquals(response, PostUpdateResponse.from(postWithImages));
        verify(postRepository, times(1)).getById(postWithImages.getId());
        verify(s3Service, times(imageFiles.size())).deletePostImage(any(String.class));
        verify(s3Service, times(0)).uploadFiles(anyList(), any(ImgType.class));
    }

    @Test
    void 게시글을_수정한다_기존_사진_없음_수정_사진_있음() {
        // Given
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("자유", "updateTitle", "updateContent");
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(s3Service.uploadFiles(imageFiles, ImgType.COMMUNITY)).thenReturn(uploadedFileUrlResponseList);

        // When
        PostUpdateResponse response = postService.updatePost(
                siteUser.getEmail(), board.getCode(), post.getId(), postUpdateRequest, imageFiles);

        // Then
        assertEquals(response, PostUpdateResponse.from(post));
        verify(postRepository, times(1)).getById(post.getId());
        verify(s3Service, times(0)).deletePostImage(any(String.class));
        verify(s3Service, times(1)).uploadFiles(imageFiles, ImgType.COMMUNITY);
    }

    @Test
    void 게시글을_수정한다_기존_사진_있음_수정_사진_있음() {
        // Given
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("자유", "updateTitle", "updateContent");
        when(postRepository.getById(postWithImages.getId())).thenReturn(postWithImages);
        when(s3Service.uploadFiles(imageFiles, ImgType.COMMUNITY)).thenReturn(uploadedFileUrlResponseList);

        // When
        PostUpdateResponse response = postService.updatePost(
                siteUser.getEmail(), board.getCode(), postWithImages.getId(), postUpdateRequest, imageFiles);

        // Then
        assertEquals(response, PostUpdateResponse.from(postWithImages));
        verify(postRepository, times(1)).getById(postWithImages.getId());
        verify(s3Service, times(imageFiles.size())).deletePostImage(any(String.class));
        verify(s3Service, times(1)).uploadFiles(imageFiles, ImgType.COMMUNITY);
    }

    @Test
    void 게시글을_수정할_때_유효한_게시판이_아니라면_예외_응답을_반환한다() {
        // Given
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("자유", "title", "content");
        String invalidBoardCode = "INVALID_CODE";

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.updatePost(siteUser.getEmail(), invalidBoardCode, post.getId(), postUpdateRequest, imageFiles));
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.INVALID_BOARD_CODE.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.INVALID_BOARD_CODE.getCode());
    }

    @Test
    void 게시글을_수정할_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidPostId = -1L;
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("자유", "title", "content");
        when(postRepository.getById(invalidPostId)).thenThrow(new CustomException(INVALID_POST_ID));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.updatePost(siteUser.getEmail(), board.getCode(), invalidPostId, postUpdateRequest, imageFiles));
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_POST_ID.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_POST_ID.getCode());
    }

    @Test
    void 게시글을_수정할_때_본인의_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidEmail = "invalidEmail@example.com";
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("자유", "title", "content");
        when(postRepository.getById(post.getId())).thenReturn(post);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.updatePost(invalidEmail, board.getCode(), post.getId(), postUpdateRequest, imageFiles));
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_POST_ACCESS.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_POST_ACCESS.getCode());
    }

    @Test
    void 게시글을_수정할_때_질문글_이라면_예외_응답을_반환한다() {
        // Given
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("자유", "title", "content");
        when(postRepository.getById(questionPost.getId())).thenReturn(questionPost);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.updatePost(siteUser.getEmail(), board.getCode(), questionPost.getId(), postUpdateRequest, imageFiles));
        assertThat(exception.getMessage())
                .isEqualTo(CAN_NOT_DELETE_OR_UPDATE_QUESTION.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(CAN_NOT_DELETE_OR_UPDATE_QUESTION.getCode());
    }


    @Test
    void 게시글을_수정할_때_파일_수가_5개를_넘는다면_예외_응답을_반환한다() {
        // Given
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("자유", "title", "content");
        when(postRepository.getById(post.getId())).thenReturn(post);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.updatePost(siteUser.getEmail(), board.getCode(), post.getId(), postUpdateRequest, imageFilesWithMoreThanFiveFiles));
        assertThat(exception.getMessage())
                .isEqualTo(CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES.getCode());
    }

    /**
     * 게시글 조회
     */
    @Test
    void 게시글을_찾는다() {
        // Given
        List<PostFindCommentResponse> commentFindResultDTOList = new ArrayList<>();
        when(postRepository.getByIdUsingEntityGraph(post.getId())).thenReturn(post);
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postLikeRepository.findPostLikeByPostAndSiteUser(post, siteUser)).thenReturn(Optional.empty());
        when(commentService.findCommentsByPostId(siteUser.getEmail(), post.getId())).thenReturn(commentFindResultDTOList);

        // When
        PostFindResponse response = postService.findPostById(siteUser.getEmail(), board.getCode(), post.getId());

        // Then
        PostFindResponse expectedResponse = PostFindResponse.from(
                post,
                true,
                false,
                PostFindBoardResponse.from(post.getBoard()),
                PostFindSiteUserResponse.from(post.getSiteUser()),
                commentFindResultDTOList,
                PostFindPostImageResponse.from(post.getPostImageList())
        );
        assertEquals(expectedResponse, response);
        verify(postRepository, times(1)).getByIdUsingEntityGraph(post.getId());
        verify(siteUserRepository, times(1)).getByEmail(siteUser.getEmail());
        verify(postLikeRepository, times(1)).findPostLikeByPostAndSiteUser(post, siteUser);
        verify(commentService, times(1)).findCommentsByPostId(siteUser.getEmail(), post.getId());
    }

    @Test
    void 게시글을_찾을_때_유효한_게시판이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidBoardCode = "INVALID_CODE";

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.findPostById(siteUser.getEmail(), invalidBoardCode, post.getId()));
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.INVALID_BOARD_CODE.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.INVALID_BOARD_CODE.getCode());
    }

    @Test
    void 게시글을_찾을_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidPostId = -1L;
        when(postRepository.getByIdUsingEntityGraph(invalidPostId)).thenThrow(new CustomException(INVALID_POST_ID));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.findPostById(siteUser.getEmail(), board.getCode(), invalidPostId));
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_POST_ID.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_POST_ID.getCode());
    }

    /**
     * 게시글 삭제
     */
    @Test
    void 게시글을_삭제한다() {
        // Give
        when(postRepository.getById(post.getId())).thenReturn(post);

        // When
        PostDeleteResponse postDeleteResponse = postService.deletePostById(siteUser.getEmail(), board.getCode(), post.getId());

        // Then
        assertEquals(postDeleteResponse.id(), post.getId());
        verify(postRepository, times(1)).getById(post.getId());
        verify(redisService, times(1)).deleteKey(redisUtils.getPostViewCountRedisKey(post.getId()));
        verify(postRepository, times(1)).deleteById(post.getId());
    }

    @Test
    void 게시글을_삭제할_때_유효한_게시판이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidBoardCode = "INVALID_CODE";

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.deletePostById(siteUser.getEmail(), invalidBoardCode, post.getId()));
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.INVALID_BOARD_CODE.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.INVALID_BOARD_CODE.getCode());
    }

    @Test
    void 게시글을_삭제할_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidPostId = -1L;
        when(postRepository.getById(invalidPostId)).thenThrow(new CustomException(INVALID_POST_ID));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.deletePostById(siteUser.getEmail(), board.getCode(), invalidPostId));
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.INVALID_POST_ID.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.INVALID_POST_ID.getCode());
    }

    @Test
    void 게시글을_삭제할_때_자신의_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidEmail = "invalidEmail@example.com";
        when(postRepository.getById(post.getId())).thenReturn(post);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.deletePostById(invalidEmail, board.getCode(), post.getId())
        );
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_POST_ACCESS.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_POST_ACCESS.getCode());
    }

    @Test
    void 게시글을_삭제할_때_질문글_이라면_예외_응답을_반환한다() {
        when(postRepository.getById(questionPost.getId())).thenReturn(questionPost);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.deletePostById(siteUser.getEmail(), board.getCode(), questionPost.getId()));
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.CAN_NOT_DELETE_OR_UPDATE_QUESTION.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.CAN_NOT_DELETE_OR_UPDATE_QUESTION.getCode());
    }

    /**
     * 게시글 좋아요
     */
    @Test
    void 게시글_좋아요를_등록한다() {
        // Given
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);

        // When
        PostLikeResponse postLikeResponse = postService.likePost(siteUser.getEmail(), board.getCode(), post.getId());

        // Then
        assertEquals(postLikeResponse, PostLikeResponse.from(post));
        verify(postLikeRepository, times(1)).save(any(PostLike.class));
    }

    @Test
    void 게시글_좋아요를_등록할_때_중복된_좋아요라면_예외_응답을_반환한다() {
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postLikeRepository.findPostLikeByPostAndSiteUser(post, siteUser)).thenReturn(Optional.of(postLike));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.likePost(siteUser.getEmail(), board.getCode(), post.getId()));
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.DUPLICATE_POST_LIKE.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.DUPLICATE_POST_LIKE.getCode());
    }

    @Test
    void 게시글_좋아요를_등록할_때_유효한_게시판이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidBoardCode = "INVALID_CODE";

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.likePost(siteUser.getEmail(), invalidBoardCode, post.getId()));
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.INVALID_BOARD_CODE.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.INVALID_BOARD_CODE.getCode());
    }

    @Test
    void 게시글_좋아요를_등록할_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidPostId = -1L;
        when(postRepository.getById(invalidPostId)).thenThrow(new CustomException(INVALID_POST_ID));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.likePost(siteUser.getEmail(), board.getCode(), invalidPostId));
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_POST_ID.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_POST_ID.getCode());
    }

    @Test
    void 게시글_좋아요를_삭제한다() {
        // Given
        Long likeCount = post.getLikeCount();
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postLikeRepository.getByPostAndSiteUser(post, siteUser)).thenReturn(postLike);

        // When
        PostDislikeResponse postDislikeResponse = postService.dislikePost(siteUser.getEmail(), board.getCode(), post.getId());

        // Then
        assertEquals(postDislikeResponse, PostDislikeResponse.from(post));
        verify(postLikeRepository, times(1)).deleteById(post.getId());
    }

    @Test
    void 게시글_좋아요를_삭제할_때_존재하지_않는_좋아요라면_예외_응답을_반환한다() {
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postLikeRepository.getByPostAndSiteUser(post, siteUser)).thenThrow(new CustomException(INVALID_POST_LIKE));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.dislikePost(siteUser.getEmail(), board.getCode(), post.getId()));
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.INVALID_POST_LIKE.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.INVALID_POST_LIKE.getCode());
    }

    @Test
    void 게시글_좋아요를_삭제할_때_유효한_게시판이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidBoardCode = "INVALID_CODE";

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.dislikePost(siteUser.getEmail(), invalidBoardCode, post.getId()));
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.INVALID_BOARD_CODE.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.INVALID_BOARD_CODE.getCode());
    }

    @Test
    void 게시글_좋아요를_삭제할_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidPostId = -1L;
        when(postRepository.getById(invalidPostId)).thenThrow(new CustomException(INVALID_POST_ID));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.dislikePost(siteUser.getEmail(), board.getCode(), invalidPostId));
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_POST_ID.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_POST_ID.getCode());
    }
}
