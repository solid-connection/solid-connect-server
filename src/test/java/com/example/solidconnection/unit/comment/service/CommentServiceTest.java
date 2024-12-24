package com.example.solidconnection.unit.comment.service;

import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.comment.domain.Comment;
import com.example.solidconnection.comment.dto.*;
import com.example.solidconnection.comment.repository.CommentRepository;
import com.example.solidconnection.comment.service.CommentService;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.repository.PostRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.solidconnection.custom.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("댓글 서비스 테스트")
class CommentServiceTest {
    @InjectMocks
    CommentService commentService;
    @Mock
    PostRepository postRepository;
    @Mock
    SiteUserRepository siteUserRepository;
    @Mock
    CommentRepository commentRepository;

    private SiteUser siteUser;
    private Board board;
    private Post post;
    private Comment parentComment;
    private Comment parentCommentWithNullContent;
    private Comment childComment;
    private Comment childCommentOfNullContentParent;


    @BeforeEach
    void setUp() {
        siteUser = createSiteUser();
        board = createBoard();
        post = createPost(board, siteUser);
        parentComment = createParentComment("parent");
        parentCommentWithNullContent = createParentComment(null);
        childComment = createChildComment(parentComment);
        childCommentOfNullContentParent = createChildComment(parentCommentWithNullContent);
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

    private Comment createParentComment(String content) {
        Comment comment = new Comment(
                content
        );
        comment.setPostAndSiteUser(post, siteUser);
        return comment;
    }

    private Comment createChildComment(Comment parentComment) {
        Comment comment = new Comment(
                "child"
        );
        comment.setParentCommentAndPostAndSiteUser(parentComment, post, siteUser);
        return comment;
    }

    /**
     * 댓글 조회
     */

    @Test
    void 특정_게시글의_댓글들을_조회한다() {
        // Given
        List<Comment> commentList = List.of(parentComment, childComment, parentCommentWithNullContent);
        when(commentRepository.findCommentTreeByPostId(post.getId())).thenReturn(commentList);

        // When
        List<PostFindCommentResponse> postFindCommentResponses = commentService.findCommentsByPostId(
                siteUser.getEmail(), post.getId());

        // Then
        List<PostFindCommentResponse> expectedResponse = commentList.stream()
                .map(comment -> PostFindCommentResponse.from(isOwner(comment, siteUser.getEmail()), comment))
                .collect(Collectors.toList());
        assertEquals(postFindCommentResponses, expectedResponse);
    }

    private Boolean isOwner(Comment comment, String email) {
        return comment.getSiteUser().getEmail().equals(email);
    }

    /**
     * 댓글 등록
     */
    @Test
    void 부모_댓글을_등록한다() {
        // Given
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(
                "parent", null
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(commentRepository.save(any(Comment.class))).thenReturn(parentComment);

        // When
        CommentCreateResponse commentCreateResponse = commentService.createComment(
                siteUser.getEmail(), post.getId(), commentCreateRequest);

        // Then
        assertEquals(commentCreateResponse, CommentCreateResponse.from(parentComment));
        verify(commentRepository, times(0))
                .getById(any(Long.class));
        verify(commentRepository, times(1))
                .save(commentCreateRequest.toEntity(siteUser, post, parentComment));
    }

    @Test
    void 자식_댓글을_등록한다() {
        // Given
        Long parentCommentId = 1L;
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(
                "child", parentCommentId
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(commentRepository.getById(parentCommentId)).thenReturn(parentComment);
        when(commentRepository.save(any(Comment.class))).thenReturn(childComment);

        // When
        CommentCreateResponse commentCreateResponse = commentService.createComment(
                siteUser.getEmail(), post.getId(), commentCreateRequest);

        // Then
        assertEquals(commentCreateResponse, CommentCreateResponse.from(childComment));
        verify(commentRepository, times(1))
                .getById(parentCommentId);
        verify(commentRepository, times(1))
                .save(commentCreateRequest.toEntity(siteUser, post, parentComment));
    }


    @Test
    void 댓글을_등록할_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidPostId = -1L;
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(
                "child", null
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postRepository.getById(invalidPostId)).thenThrow(new CustomException(INVALID_POST_ID));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                commentService.createComment(siteUser.getEmail(), invalidPostId, commentCreateRequest)
        );
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_POST_ID.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_POST_ID.getCode());
        verify(commentRepository, times(0))
                .save(any(Comment.class));
    }

    @Test
    void 댓글을_등록할_때_유효한_부모_댓글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidParentCommentId = -1L;
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(
                "child", invalidParentCommentId
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(commentRepository.getById(invalidParentCommentId)).thenThrow(new CustomException(INVALID_COMMENT_ID));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                commentService.createComment(siteUser.getEmail(), post.getId(), commentCreateRequest)
        );
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_COMMENT_ID.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_COMMENT_ID.getCode());
        verify(commentRepository, times(0))
                .save(any(Comment.class));
    }

    @Test
    void 댓글을_등록할_때_대대댓글_부터는_예외_응답을_반환한다() {
        // Given
        Long childCommentId = 1L;
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(
                "child's child", childCommentId
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(commentRepository.getById(childCommentId)).thenReturn(childComment);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                commentService.createComment(siteUser.getEmail(), post.getId(), commentCreateRequest)
        );
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_COMMENT_LEVEL.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_COMMENT_LEVEL.getCode());
        verify(commentRepository, times(0))
                .save(any(Comment.class));
    }

    /**
     * 댓글 수정
     */
    @Test
    void 댓글을_수정한다() {
        // Given
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest(
                "update"
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(commentRepository.getById(any())).thenReturn(parentComment);

        // When
        CommentUpdateResponse commentUpdateResponse = commentService.updateComment(
                siteUser.getEmail(), post.getId(), parentComment.getId(), commentUpdateRequest);

        // Then
        assertEquals(commentUpdateResponse.id(), parentComment.getId());
    }

    @Test
    void 댓글을_수정할_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidPostId = -1L;
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest(
                "update"
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postRepository.getById(invalidPostId)).thenThrow(new CustomException(INVALID_POST_ID));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                commentService.updateComment(siteUser.getEmail(), invalidPostId, parentComment.getId(), commentUpdateRequest)
        );
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_POST_ID.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_POST_ID.getCode());
    }

    @Test
    void 댓글을_수정할_때_유효한_댓글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidCommentId = -1L;
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest(
                "update"
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(commentRepository.getById(invalidCommentId)).thenThrow(new CustomException(INVALID_COMMENT_ID));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                commentService.updateComment(siteUser.getEmail(), post.getId(), invalidCommentId, commentUpdateRequest)
        );
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_COMMENT_ID.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_COMMENT_ID.getCode());
    }

    @Test
    void 댓글을_수정할_때_이미_삭제된_댓글이라면_예외_응답을_반환한다() {
        // Given
        parentComment.deprecateComment();
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest(
                "update"
        );
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(commentRepository.getById(any())).thenReturn(parentComment);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                commentService.updateComment(siteUser.getEmail(), post.getId(), parentComment.getId(), commentUpdateRequest)
        );
        assertThat(exception.getMessage())
                .isEqualTo(CAN_NOT_UPDATE_DEPRECATED_COMMENT.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(CAN_NOT_UPDATE_DEPRECATED_COMMENT.getCode());
    }

    @Test
    void 댓글을_수정할_때_자신의_댓글이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidEmail = "invalidEmail@test.com";
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest(
                "update"
        );
        when(siteUserRepository.getByEmail(invalidEmail)).thenReturn(siteUser);
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(commentRepository.getById(any())).thenReturn(parentComment);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                commentService.updateComment(invalidEmail, post.getId(), parentComment.getId(), commentUpdateRequest)
        );
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_POST_ACCESS.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_POST_ACCESS.getCode());
    }

    /**
     * 댓글 삭제
     */

    @Test
    void 댓글을_삭제한다_자식댓글_있음() {
        // Given
        Long parentCommentId = 1L;
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(commentRepository.getById(any())).thenReturn(parentComment);

        // When
        CommentDeleteResponse commentDeleteResponse = commentService.deleteCommentById(
                siteUser.getEmail(), post.getId(), parentCommentId);

        // Then
        assertEquals(parentComment.getContent(), null);
        assertEquals(commentDeleteResponse.id(), parentCommentId);
        verify(commentRepository, times(0)).deleteById(parentCommentId);
    }

    @Test
    void 댓글을_삭제한다_자식댓글_없음() {
        // Given
        Long childCommentId = 1L;
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(commentRepository.getById(any())).thenReturn(childComment);

        // When
        CommentDeleteResponse commentDeleteResponse = commentService.deleteCommentById(
                siteUser.getEmail(), post.getId(), childCommentId);

        // Then
        assertEquals(commentDeleteResponse.id(), childCommentId);
        verify(commentRepository, times(1)).deleteById(childCommentId);
    }

    @Test
    void 대댓글을_삭제한다_부모댓글_유효() {
        // Given
        Long childCommentId = 1L;
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(commentRepository.getById(any())).thenReturn(childComment);

        // When
        CommentDeleteResponse commentDeleteResponse = commentService.deleteCommentById(
                siteUser.getEmail(), post.getId(), childCommentId);

        // Then
        assertEquals(commentDeleteResponse.id(), childCommentId);
        verify(commentRepository, times(1)).deleteById(childCommentId);
    }

    @Test
    void 대댓글을_삭제한다_부모댓글_유효하지_않음() {
        // Given

        Long childCommentId = 1L;
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(commentRepository.getById(any())).thenReturn(childCommentOfNullContentParent);

        // When
        CommentDeleteResponse commentDeleteResponse = commentService.deleteCommentById(
                siteUser.getEmail(), post.getId(), childCommentId);

        // Then
        assertEquals(commentDeleteResponse.id(), childCommentId);
        verify(commentRepository, times(2)).deleteById(any());
    }

    @Test
    void 댓글을_삭제할_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidPostId = -1L;
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postRepository.getById(invalidPostId)).thenThrow(new CustomException(INVALID_POST_ID));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                commentService.deleteCommentById(siteUser.getEmail(), invalidPostId, parentComment.getId())
        );
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_POST_ID.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_POST_ID.getCode());
    }

    @Test
    void 댓글을_삭제할_때_유효한_댓글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidCommentId = -1L;
        when(siteUserRepository.getByEmail(siteUser.getEmail())).thenReturn(siteUser);
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(commentRepository.getById(invalidCommentId)).thenThrow(new CustomException(INVALID_COMMENT_ID));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                commentService.deleteCommentById(siteUser.getEmail(), post.getId(), invalidCommentId)
        );
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_COMMENT_ID.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_COMMENT_ID.getCode());
    }

    @Test
    void 댓글을_삭제할_때_자신의_댓글이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidEmail = "invalidEmail@test.com";
        when(siteUserRepository.getByEmail(invalidEmail)).thenReturn(siteUser);
        when(postRepository.getById(post.getId())).thenReturn(post);
        when(commentRepository.getById(any())).thenReturn(parentComment);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                commentService.deleteCommentById(invalidEmail, post.getId(), parentComment.getId())
        );
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_POST_ACCESS.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_POST_ACCESS.getCode());
    }
}
