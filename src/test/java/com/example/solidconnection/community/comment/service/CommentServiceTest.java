package com.example.solidconnection.community.comment.service;

import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.community.comment.domain.Comment;
import com.example.solidconnection.community.comment.dto.CommentCreateRequest;
import com.example.solidconnection.community.comment.dto.CommentCreateResponse;
import com.example.solidconnection.community.comment.dto.CommentDeleteResponse;
import com.example.solidconnection.community.comment.dto.CommentUpdateRequest;
import com.example.solidconnection.community.comment.dto.CommentUpdateResponse;
import com.example.solidconnection.community.comment.dto.PostFindCommentResponse;
import com.example.solidconnection.community.comment.repository.CommentRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.type.PostCategory;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.CAN_NOT_UPDATE_DEPRECATED_COMMENT;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_COMMENT_ID;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_COMMENT_LEVEL;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_ACCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("댓글 서비스 테스트")
class CommentServiceTest extends BaseIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Nested
    class 댓글_조회_테스트 {

        @Test
        void 게시글의_모든_댓글을_조회한다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1);
            Comment parentComment = createComment(testPost, 테스트유저_1, "부모 댓글");
            Comment childComment = createChildComment(testPost, 테스트유저_2, parentComment, "자식 댓글");
            List<Comment> comments = List.of(parentComment, childComment);

            // when
            List<PostFindCommentResponse> responses = commentService.findCommentsByPostId(
                    테스트유저_1,
                    testPost.getId()
            );

            // then
            assertAll(
                    () -> assertThat(responses).hasSize(comments.size()),
                    () -> assertThat(responses)
                            .filteredOn(response -> response.id().equals(parentComment.getId()))
                            .singleElement()
                            .satisfies(response -> assertAll(
                                    () -> assertThat(response.id()).isEqualTo(parentComment.getId()),
                                    () -> assertThat(response.parentId()).isNull(),
                                    () -> assertThat(response.content()).isEqualTo(parentComment.getContent()),
                                    () -> assertThat(response.isOwner()).isTrue(),
                                    () -> assertThat(response.createdAt()).isEqualTo(parentComment.getCreatedAt()),
                                    () -> assertThat(response.updatedAt()).isEqualTo(parentComment.getUpdatedAt()),

                                    () -> assertThat(response.postFindSiteUserResponse().id())
                                            .isEqualTo(parentComment.getSiteUser().getId()),
                                    () -> assertThat(response.postFindSiteUserResponse().nickname())
                                            .isEqualTo(parentComment.getSiteUser().getNickname()),
                                    () -> assertThat(response.postFindSiteUserResponse().profileImageUrl())
                                            .isEqualTo(parentComment.getSiteUser().getProfileImageUrl())
                            )),
                    () -> assertThat(responses)
                            .filteredOn(response -> response.id().equals(childComment.getId()))
                            .singleElement()
                            .satisfies(response -> assertAll(
                                    () -> assertThat(response.id()).isEqualTo(childComment.getId()),
                                    () -> assertThat(response.parentId()).isEqualTo(parentComment.getId()),
                                    () -> assertThat(response.content()).isEqualTo(childComment.getContent()),
                                    () -> assertThat(response.isOwner()).isFalse(),
                                    () -> assertThat(response.createdAt()).isEqualTo(childComment.getCreatedAt()),
                                    () -> assertThat(response.updatedAt()).isEqualTo(childComment.getUpdatedAt()),

                                    () -> assertThat(response.postFindSiteUserResponse().id())
                                            .isEqualTo(childComment.getSiteUser().getId()),
                                    () -> assertThat(response.postFindSiteUserResponse().nickname())
                                            .isEqualTo(childComment.getSiteUser().getNickname()),
                                    () -> assertThat(response.postFindSiteUserResponse().profileImageUrl())
                                            .isEqualTo(childComment.getSiteUser().getProfileImageUrl())
                            ))
            );
        }
    }

    @Nested
    class 댓글_생성_테스트 {

        @Test
        void 댓글을_성공적으로_생성한다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1);
            CommentCreateRequest request = new CommentCreateRequest("테스트 댓글", null);

            // when
            CommentCreateResponse response = commentService.createComment(
                    테스트유저_1,
                    testPost.getId(),
                    request
            );

            // then
            Comment savedComment = commentRepository.findById(response.id()).orElseThrow();
            assertAll(
                    () -> assertThat(savedComment.getId()).isEqualTo(response.id()),
                    () -> assertThat(savedComment.getContent()).isEqualTo(request.content()),
                    () -> assertThat(savedComment.getParentComment()).isNull(),
                    () -> assertThat(savedComment.getPost().getId()).isEqualTo(testPost.getId()),
                    () -> assertThat(savedComment.getSiteUser().getId()).isEqualTo(테스트유저_1.getId())
            );
        }

        @Test
        void 대댓글을_성공적으로_생성한다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1);
            Comment parentComment = createComment(testPost, 테스트유저_1, "부모 댓글");
            CommentCreateRequest request = new CommentCreateRequest("테스트 대댓글", parentComment.getId());

            // when
            CommentCreateResponse response = commentService.createComment(
                    테스트유저_2,
                    testPost.getId(),
                    request
            );

            // then
            Comment savedComment = commentRepository.findById(response.id()).orElseThrow();
            assertAll(
                    () -> assertThat(savedComment.getId()).isEqualTo(response.id()),
                    () -> assertThat(savedComment.getContent()).isEqualTo(request.content()),
                    () -> assertThat(savedComment.getParentComment().getId()).isEqualTo(parentComment.getId()),
                    () -> assertThat(savedComment.getPost().getId()).isEqualTo(testPost.getId()),
                    () -> assertThat(savedComment.getSiteUser().getId()).isEqualTo(테스트유저_2.getId())
            );
        }

        @Test
        void 대대댓글_생성_시도하면_예외_응답을_반환한다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1);
            Comment parentComment = createComment(testPost, 테스트유저_1, "부모 댓글");
            Comment childComment = createChildComment(testPost, 테스트유저_2, parentComment, "자식 댓글");
            CommentCreateRequest request = new CommentCreateRequest("테스트 대대댓글", childComment.getId());

            // when & then
            assertThatThrownBy(() ->
                    commentService.createComment(
                            테스트유저_1,
                            testPost.getId(),
                            request
                    ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_COMMENT_LEVEL.getMessage());
        }

        @Test
        void 존재하지_않는_부모댓글로_대댓글_작성시_예외를_반환한다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1);
            long invalidCommentId = 9999L;
            CommentCreateRequest request = new CommentCreateRequest("테스트 대댓글", invalidCommentId);

            // when & then
            assertThatThrownBy(() ->
                    commentService.createComment(
                            테스트유저_1,
                            testPost.getId(),
                            request
                    ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_COMMENT_ID.getMessage());
        }
    }

    @Nested
    class 댓글_수정_테스트 {

        @Test
        void 댓글을_성공적으로_수정한다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1);
            Comment comment = createComment(testPost, 테스트유저_1, "원본 댓글");
            CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");

            // when
            CommentUpdateResponse response = commentService.updateComment(
                    테스트유저_1,
                    testPost.getId(),
                    comment.getId(),
                    request
            );

            // then
            Comment updatedComment = commentRepository.findById(response.id()).orElseThrow();
            assertAll(
                    () -> assertThat(updatedComment.getId()).isEqualTo(comment.getId()),
                    () -> assertThat(updatedComment.getContent()).isEqualTo(request.content()),
                    () -> assertThat(updatedComment.getParentComment()).isNull(),
                    () -> assertThat(updatedComment.getPost().getId()).isEqualTo(testPost.getId()),
                    () -> assertThat(updatedComment.getSiteUser().getId()).isEqualTo(테스트유저_1.getId())
            );
        }

        @Test
        void 다른_사용자의_댓글을_수정하면_예외_응답을_반환한다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1);
            Comment comment = createComment(testPost, 테스트유저_1, "원본 댓글");
            CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");

            // when & then
            assertThatThrownBy(() ->
                    commentService.updateComment(
                            테스트유저_2,
                            testPost.getId(),
                            comment.getId(),
                            request
                    ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_ACCESS.getMessage());
        }

        @Test
        void 삭제된_댓글을_수정하면_예외_응답을_반환한다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1);
            Comment comment = createComment(testPost, 테스트유저_1, null);
            CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");

            // when & then
            assertThatThrownBy(() ->
                    commentService.updateComment(
                            테스트유저_1,
                            testPost.getId(),
                            comment.getId(),
                            request
                    ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(CAN_NOT_UPDATE_DEPRECATED_COMMENT.getMessage());
        }
    }

    @Nested
    class 댓글_삭제_테스트 {

        @Test
        @Transactional
        void 대댓글이_없는_댓글을_삭제한다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1);
            Comment comment = createComment(testPost, 테스트유저_1, "테스트 댓글");
            List<Comment> comments = testPost.getCommentList();
            int expectedCommentsCount = comments.size() - 1;

            // when
            CommentDeleteResponse response = commentService.deleteCommentById(
                    테스트유저_1,
                    testPost.getId(),
                    comment.getId()
            );

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(comment.getId()),
                    () -> assertThat(commentRepository.findById(comment.getId())).isEmpty(),
                    () -> assertThat(testPost.getCommentList()).hasSize(expectedCommentsCount)
            );
        }

        @Test
        @Transactional
        void 대댓글이_있는_댓글을_삭제하면_내용만_삭제된다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1);
            Comment parentComment = createComment(testPost, 테스트유저_1, "부모 댓글");
            Comment childComment = createChildComment(testPost, 테스트유저_2, parentComment, "자식 댓글");
            List<Comment> comments = testPost.getCommentList();
            List<Comment> childComments = parentComment.getCommentList();

            // when
            CommentDeleteResponse response = commentService.deleteCommentById(
                    테스트유저_1,
                    testPost.getId(),
                    parentComment.getId()
            );

            // then
            Comment deletedComment = commentRepository.findById(response.id()).orElseThrow();
            assertAll(
                    () -> assertThat(deletedComment.getContent()).isNull(),
                    () -> assertThat(deletedComment.getCommentList())
                            .extracting(Comment::getId)
                            .containsExactlyInAnyOrder(childComment.getId()),
                    () -> assertThat(testPost.getCommentList()).hasSize(comments.size()),
                    () -> assertThat(deletedComment.getCommentList()).hasSize(childComments.size())
            );
        }

        @Test
        @Transactional
        void 대댓글을_삭제하면_부모댓글이_삭제되지_않는다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1);
            Comment parentComment = createComment(testPost, 테스트유저_1, "부모 댓글");
            Comment childComment1 = createChildComment(testPost, 테스트유저_2, parentComment, "자식 댓글 1");
            Comment childComment2 = createChildComment(testPost, 테스트유저_2, parentComment, "자식 댓글 2");
            List<Comment> childComments = parentComment.getCommentList();
            int expectedChildCommentsCount = childComments.size() - 1;

            // when
            CommentDeleteResponse response = commentService.deleteCommentById(
                    테스트유저_2,
                    testPost.getId(),
                    childComment1.getId()
            );

            // then
            Comment remainingParentComment = commentRepository.findById(parentComment.getId()).orElseThrow();
            List<Comment> remainingChildComments = remainingParentComment.getCommentList();
            assertAll(
                    () -> assertThat(commentRepository.findById(response.id())).isEmpty(),
                    () -> assertThat(remainingParentComment.getContent()).isEqualTo(parentComment.getContent()),
                    () -> assertThat(remainingChildComments).hasSize(expectedChildCommentsCount),
                    () -> assertThat(remainingChildComments)
                            .extracting(Comment::getId)
                            .containsExactly(childComment2.getId())
            );
        }

        @Test
        @Transactional
        void 대댓글을_삭제하고_부모댓글이_삭제된_상태면_부모댓글도_삭제된다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1);
            Comment parentComment = createComment(testPost, 테스트유저_1, "부모 댓글");
            Comment childComment = createChildComment(testPost, 테스트유저_2, parentComment, "자식 댓글");
            List<Comment> comments = testPost.getCommentList();
            int expectedCommentsCount = comments.size() - 2;
            parentComment.deprecateComment();

            // when
            CommentDeleteResponse response = commentService.deleteCommentById(
                    테스트유저_2,
                    testPost.getId(),
                    childComment.getId()
            );

            // then
            assertAll(
                    () -> assertThat(commentRepository.findById(response.id())).isEmpty(),
                    () -> assertThat(commentRepository.findById(parentComment.getId())).isEmpty(),
                    () -> assertThat(testPost.getCommentList()).hasSize(expectedCommentsCount)
            );
        }

        @Test
        void 다른_사용자의_댓글을_삭제하면_예외_응답을_반환한다() {
            // given
            Post testPost = createPost(자유게시판, 테스트유저_1);
            Comment comment = createComment(testPost, 테스트유저_1, "테스트 댓글");

            // when & then
            assertThatThrownBy(() ->
                    commentService.deleteCommentById(
                            테스트유저_2,
                            testPost.getId(),
                            comment.getId()
                    ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_ACCESS.getMessage());
        }
    }

    private Post createPost(Board board, SiteUser siteUser) {
        Post post = new Post(
                "테스트 제목",
                "테스트 내용",
                false,
                0L,
                0L,
                PostCategory.자유
        );
        post.setBoardAndSiteUser(board, siteUser);
        return postRepository.save(post);
    }

    private Comment createComment(Post post, SiteUser siteUser, String content) {
        Comment comment = new Comment(content);
        comment.setPostAndSiteUser(post, siteUser);
        return commentRepository.save(comment);
    }

    private Comment createChildComment(Post post, SiteUser siteUser, Comment parentComment, String content) {
        Comment comment = new Comment(content);
        comment.setPostAndSiteUser(post, siteUser);
        comment.setParentCommentAndPostAndSiteUser(parentComment, post, siteUser);
        return commentRepository.save(comment);
    }
}
