package com.example.solidconnection.community.comment.service;

import static com.example.solidconnection.common.exception.ErrorCode.CAN_NOT_UPDATE_DEPRECATED_COMMENT;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_COMMENT_ID;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_COMMENT_LEVEL;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_POST_ACCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.community.board.fixture.BoardFixture;
import com.example.solidconnection.community.comment.domain.Comment;
import com.example.solidconnection.community.comment.dto.CommentCreateRequest;
import com.example.solidconnection.community.comment.dto.CommentCreateResponse;
import com.example.solidconnection.community.comment.dto.CommentDeleteResponse;
import com.example.solidconnection.community.comment.dto.CommentUpdateRequest;
import com.example.solidconnection.community.comment.dto.CommentUpdateResponse;
import com.example.solidconnection.community.comment.dto.PostFindCommentResponse;
import com.example.solidconnection.community.comment.fixture.CommentFixture;
import com.example.solidconnection.community.comment.repository.CommentRepository;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostCategory;
import com.example.solidconnection.community.post.fixture.PostFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.PostFindSiteUserResponse;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.siteuser.fixture.UserBlockFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("댓글 서비스 테스트")
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private BoardFixture boardFixture;

    @Autowired
    private PostFixture postFixture;

    @Autowired
    private CommentFixture commentFixture;

    @Autowired
    private UserBlockFixture userBlockFixture;

    private SiteUser user1;
    private SiteUser user2;
    private Post post;

    @BeforeEach
    void setUp() {
        user1 = siteUserFixture.사용자(1, "test1");
        user2 = siteUserFixture.사용자(2, "test2");
        post = postFixture.게시글(
                "제목1",
                "내용1",
                false,
                PostCategory.자유,
                boardFixture.자유게시판(),
                user1
        );
    }

    @Nested
    class 댓글_조회_테스트 {

        @Test
        void 게시글의_모든_댓글을_조회한다() {
            // given
            Comment parentComment = commentFixture.부모_댓글("부모 댓글", post, user1);
            Comment childComment = commentFixture.자식_댓글("자식 댓글 1", post, user2, parentComment);
            List<Comment> comments = List.of(parentComment, childComment);

            // when
            List<PostFindCommentResponse> responses = commentService.findCommentsByPostId(user1.getId(), post.getId());

            // then
            assertAll(
                    () -> assertThat(responses).hasSize(comments.size()),
                    () -> assertThat(responses)
                            .filteredOn(response -> response.id().equals(parentComment.getId()))
                            .singleElement()
                            .satisfies(response -> assertAll(
                                    () -> assertThat(response.id()).isEqualTo(parentComment.getId()),
                                    () -> assertThat(response.parentId()).isNull(),
                                    () -> assertThat(response.isOwner()).isTrue()
                            )),
                    () -> assertThat(responses)
                            .filteredOn(response -> response.id().equals(childComment.getId()))
                            .singleElement()
                            .satisfies(response -> assertAll(
                                    () -> assertThat(response.id()).isEqualTo(childComment.getId()),
                                    () -> assertThat(response.parentId()).isEqualTo(parentComment.getId()),
                                    () -> assertThat(response.isOwner()).isFalse()
                            ))
            );
        }

        @Test
        void 부모댓글과_대댓글이_모두_삭제되면_응답에서_제외한다() {
            // given
            Comment parentComment = commentFixture.부모_댓글("부모 댓글", post, user1);
            Comment childComment1 = commentFixture.자식_댓글("자식 댓글1", post, user2, parentComment);
            Comment childComment2 = commentFixture.자식_댓글("자식 댓글2", post, user2, parentComment);

            parentComment.deprecateComment();
            childComment1.deprecateComment();
            childComment2.deprecateComment();
            commentRepository.saveAll(List.of(parentComment, childComment1, childComment2));

            // when
            List<PostFindCommentResponse> responses = commentService.findCommentsByPostId(user1.getId(), post.getId());

            // then
            assertAll(
                    () -> assertThat(responses).isEmpty()
            );
        }

        @Test
        void 부모댓글이_삭제된_경우에도_자식댓글이_존재하면_자식댓글의_내용만_반환한다() {
            // given
            Comment parentComment = commentFixture.부모_댓글("부모 댓글", post, user1);
            Comment childComment1 = commentFixture.자식_댓글("자식 댓글1", post, user2, parentComment);
            Comment childComment2 = commentFixture.자식_댓글("자식 댓글2", post, user2, parentComment);

            parentComment.deprecateComment();
            commentRepository.saveAll(List.of(parentComment, childComment1, childComment2));

            // when
            List<PostFindCommentResponse> responses = commentService.findCommentsByPostId(user1.getId(), post.getId());

            // then
            assertAll(
                    () -> assertThat(responses).hasSize(3),
                    () -> assertThat(responses)
                            .extracting(PostFindCommentResponse::id)
                            .containsExactlyInAnyOrder(parentComment.getId(), childComment1.getId(), childComment2.getId()),
                    () -> assertThat(responses)
                            .filteredOn(response -> response.id().equals(parentComment.getId()))
                            .extracting(PostFindCommentResponse::content)
                            .containsExactly(""),
                    () -> assertThat(responses)
                            .filteredOn(response -> !response.id().equals(parentComment.getId()))
                            .extracting(PostFindCommentResponse::content)
                            .containsExactlyInAnyOrder("자식 댓글1", "자식 댓글2")
            );
        }

        @Test
        void 부모댓글이_삭제된_경우_부모댓글의_사용자정보는_null이고_자식댓글의_사용자정보는_정상적으로_반환한다() {
            // given
            Comment parentComment = commentFixture.부모_댓글("부모 댓글", post, user1);
            Comment childComment1 = commentFixture.자식_댓글("자식 댓글1", post, user2, parentComment);
            Comment childComment2 = commentFixture.자식_댓글("자식 댓글2", post, user2, parentComment);

            parentComment.deprecateComment();
            commentRepository.saveAll(List.of(parentComment, childComment1, childComment2));

            // when
            List<PostFindCommentResponse> responses = commentService.findCommentsByPostId(user1.getId(), post.getId());

            // then
            assertAll(
                    () -> assertThat(responses)
                            .filteredOn(response -> response.id().equals(parentComment.getId()))
                            .extracting(PostFindCommentResponse::postFindSiteUserResponse)
                            .containsExactly((PostFindSiteUserResponse) null),
                    () -> assertThat(responses)
                            .filteredOn(response -> !response.id().equals(parentComment.getId()))
                            .extracting(PostFindCommentResponse::postFindSiteUserResponse)
                            .isNotNull()
                            .extracting(PostFindSiteUserResponse::id)
                            .containsExactlyInAnyOrder(user2.getId(), user2.getId())
            );
        }

        @Test
        void 차단한_사용자의_댓글은_제외된다() {
            // given
            userBlockFixture.유저_차단(user1.getId(), user2.getId());
            Comment parentComment1 = commentFixture.부모_댓글("부모 댓글1", post, user1);
            Comment childComment1 = commentFixture.자식_댓글("자식 댓글1", post, user1, parentComment1);
            Comment childComment2 = commentFixture.자식_댓글("자식 댓글2", post, user2, parentComment1);
            Comment parentCommen2 = commentFixture.부모_댓글("부모 댓글2", post, user2);
            Comment childComment3 = commentFixture.자식_댓글("자식 댓글1", post, user1, parentCommen2);
            Comment childComment4 = commentFixture.자식_댓글("자식 댓글1", post, user1, parentCommen2);


            // when
            List<PostFindCommentResponse> responses = commentService.findCommentsByPostId(user1.getId(), post.getId());

            // then
            assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses)
                        .extracting(PostFindCommentResponse::id)
                        .containsExactly(parentComment1.getId(), childComment1.getId()),
                () -> assertThat(responses)
                        .extracting(PostFindCommentResponse::id)
                        .doesNotContain(childComment2.getId(), parentCommen2.getId(), childComment3.getId(), childComment4.getId()))
            ;
        }
    }

    @Nested
    class 댓글_생성_테스트 {

        @Test
        void 댓글을_성공적으로_생성한다() {
            // given
            CommentCreateRequest request = new CommentCreateRequest(post.getId(), "댓글", null);

            // when
            CommentCreateResponse response = commentService.createComment(user1.getId(), request);

            // then
            Comment savedComment = commentRepository.findById(response.id()).orElseThrow();
            assertAll(
                    () -> assertThat(savedComment.getId()).isEqualTo(response.id()),
                    () -> assertThat(savedComment.getContent()).isEqualTo(request.content()),
                    () -> assertThat(savedComment.getParentComment()).isNull(),
                    () -> assertThat(savedComment.getPost().getId()).isEqualTo(post.getId()),
                    () -> assertThat(savedComment.getSiteUserId()).isEqualTo(user1.getId())
            );
        }

        @Test
        void 대댓글을_성공적으로_생성한다() {
            // given
            Comment parentComment = commentFixture.부모_댓글("부모 댓글", post, user1);
            CommentCreateRequest request = new CommentCreateRequest(post.getId(), "자식 댓글", parentComment.getId());

            // when
            CommentCreateResponse response = commentService.createComment(user2.getId(), request);

            // then
            Comment savedComment = commentRepository.findById(response.id()).orElseThrow();
            assertAll(
                    () -> assertThat(savedComment.getId()).isEqualTo(response.id()),
                    () -> assertThat(savedComment.getContent()).isEqualTo(request.content()),
                    () -> assertThat(savedComment.getParentComment().getId()).isEqualTo(parentComment.getId()),
                    () -> assertThat(savedComment.getPost().getId()).isEqualTo(post.getId()),
                    () -> assertThat(savedComment.getSiteUserId()).isEqualTo(user2.getId())
            );
        }

        @Test
        void 대대댓글_생성_시도하면_예외가_발생한다() {
            // given
            Comment parentComment = commentFixture.부모_댓글("부모 댓글", post, user1);
            Comment childComment = commentFixture.자식_댓글("자식 댓글", post, user2, parentComment);
            CommentCreateRequest request = new CommentCreateRequest(post.getId(), "대대댓글", childComment.getId());

            // when & then
            assertThatThrownBy(() ->
                                       commentService.createComment(
                                               user1.getId(),
                                               request
                                       ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_COMMENT_LEVEL.getMessage());
        }

        @Test
        void 존재하지_않는_부모댓글로_대댓글_작성시_예외가_빌생한다() {
            // given
            long invalidCommentId = 9999L;
            CommentCreateRequest request = new CommentCreateRequest(post.getId(), "자식 댓글", invalidCommentId);

            // when & then
            assertThatThrownBy(() ->
                                       commentService.createComment(
                                               user1.getId(),
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
            Comment comment = commentFixture.부모_댓글("원본 댓글", post, user1);
            CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");

            // when
            CommentUpdateResponse response = commentService.updateComment(user1.getId(), comment.getId(), request);

            // then
            Comment updatedComment = commentRepository.findById(response.id()).orElseThrow();
            assertAll(
                    () -> assertThat(updatedComment.getId()).isEqualTo(comment.getId()),
                    () -> assertThat(updatedComment.getContent()).isEqualTo(request.content()),
                    () -> assertThat(updatedComment.getParentComment()).isNull(),
                    () -> assertThat(updatedComment.getPost().getId()).isEqualTo(post.getId()),
                    () -> assertThat(updatedComment.getSiteUserId()).isEqualTo(user1.getId())
            );
        }

        @Test
        void 다른_사용자의_댓글을_수정하면_예외가_발생한다() {
            // given
            Comment comment = commentFixture.부모_댓글("원본 댓글", post, user1);
            CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");

            // when & then
            assertThatThrownBy(() ->
                                       commentService.updateComment(
                                               user2.getId(),
                                               comment.getId(),
                                               request
                                       ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_ACCESS.getMessage());
        }

        @Test
        void 삭제된_댓글을_수정하면_예외가_발생한다() {
            // given
            Comment comment = commentFixture.부모_댓글(null, post, user1);
            CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");

            // when & then
            assertThatThrownBy(() ->
                                       commentService.updateComment(
                                               user1.getId(),
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
            Comment comment = commentFixture.부모_댓글("부모 댓글", post, user1);
            List<Comment> comments = post.getCommentList();
            int expectedCommentsCount = comments.size() - 1;

            // when
            CommentDeleteResponse response = commentService.deleteCommentById(user1.getId(), comment.getId());

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(comment.getId()),
                    () -> assertThat(commentRepository.findById(comment.getId())).isEmpty(),
                    () -> assertThat(post.getCommentList()).hasSize(expectedCommentsCount)
            );
        }

        @Test
        @Transactional
        void 대댓글이_있는_댓글을_삭제하면_내용만_삭제된다() {
            // given
            Comment parentComment = commentFixture.부모_댓글("부모 댓글", post, user1);
            Comment childComment = commentFixture.자식_댓글("자식 댓글", post, user2, parentComment);
            List<Comment> comments = post.getCommentList();
            List<Comment> childComments = parentComment.getCommentList();

            // when
            CommentDeleteResponse response = commentService.deleteCommentById(user1.getId(), parentComment.getId());

            // then
            Comment deletedComment = commentRepository.findById(response.id()).orElseThrow();
            assertAll(
                    () -> assertThat(deletedComment.getContent()).isEqualTo("부모 댓글"),
                    () -> assertThat(deletedComment.isDeleted()).isTrue(),
                    () -> assertThat(deletedComment.getCommentList())
                            .extracting(Comment::getId)
                            .containsExactlyInAnyOrder(childComment.getId()),
                    () -> assertThat(post.getCommentList()).hasSize(comments.size()),
                    () -> assertThat(deletedComment.getCommentList()).hasSize(childComments.size())
            );
        }

        @Test
        @Transactional
        void 대댓글을_삭제하면_부모댓글이_삭제되지_않는다() {
            // given
            Comment parentComment = commentFixture.부모_댓글("부모 댓글", post, user1);
            Comment childComment1 = commentFixture.자식_댓글("자식 댓글1", post, user2, parentComment);
            Comment childComment2 = commentFixture.자식_댓글("자식 댓글2", post, user2, parentComment);
            List<Comment> childComments = parentComment.getCommentList();
            int expectedChildCommentsCount = childComments.size() - 1;

            // when
            CommentDeleteResponse response = commentService.deleteCommentById(user2.getId(), childComment1.getId());

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
        void 다른_사용자의_댓글을_삭제하면_예외가_발생한다() {
            // given
            Comment comment = commentFixture.부모_댓글("부모 댓글", post, user1);

            // when & then
            assertThatThrownBy(() ->
                                       commentService.deleteCommentById(
                                               user2.getId(),
                                               comment.getId()
                                       ))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_POST_ACCESS.getMessage());
        }
    }
}
