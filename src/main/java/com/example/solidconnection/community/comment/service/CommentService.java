package com.example.solidconnection.community.comment.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.community.comment.domain.Comment;
import com.example.solidconnection.community.comment.dto.CommentCreateRequest;
import com.example.solidconnection.community.comment.dto.CommentCreateResponse;
import com.example.solidconnection.community.comment.dto.CommentDeleteResponse;
import com.example.solidconnection.community.comment.dto.CommentUpdateRequest;
import com.example.solidconnection.community.comment.dto.CommentUpdateResponse;
import com.example.solidconnection.community.comment.dto.PostFindCommentResponse;
import com.example.solidconnection.community.comment.repository.CommentRepository;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.solidconnection.common.exception.ErrorCode.CAN_NOT_UPDATE_DEPRECATED_COMMENT;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_COMMENT_LEVEL;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_POST_ACCESS;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final SiteUserRepository siteUserRepository;

    @Transactional(readOnly = true)
    public List<PostFindCommentResponse> findCommentsByPostId(SiteUser siteUser, Long postId) {
        SiteUser commentOwner = siteUserRepository.findById(siteUser.getId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        List<Comment> allComments = commentRepository.findCommentTreeByPostId(postId);
        List<Comment> filteredComments = filterCommentsByDeletionRules(allComments);

        return filteredComments.stream()
                .map(comment -> PostFindCommentResponse.from(
                        isOwner(comment, siteUser), comment, commentOwner))
                .collect(Collectors.toList());
    }

    private List<Comment> filterCommentsByDeletionRules(List<Comment> comments) {
        Map<Long, List<Comment>> commentsByParent = comments.stream()
                .filter(comment -> comment.getParentComment() != null)
                .collect(Collectors.groupingBy(comment -> comment.getParentComment().getId()));

        List<Comment> result = new ArrayList<>();

        List<Comment> parentComments = comments.stream()
                .filter(comment -> comment.getParentComment() == null)
                .toList();
        for (Comment parent : parentComments) {
            List<Comment> children = commentsByParent.getOrDefault(parent.getId(), List.of());
            boolean allDeleted = parent.isDeleted() &&
                    children.stream().allMatch(Comment::isDeleted);
            if (!allDeleted) {
                result.add(parent);
                result.addAll(children.stream()
                        .filter(child -> !child.isDeleted())
                        .toList());
            }
        }
        return result;
    }

    private Boolean isOwner(Comment comment, SiteUser siteUser) {
        return Objects.equals(comment.getSiteUserId(), siteUser.getId());
    }

    @Transactional
    public CommentCreateResponse createComment(SiteUser siteUser, CommentCreateRequest commentCreateRequest) {
        Post post = postRepository.getById(commentCreateRequest.postId());

        Comment parentComment = null;
        if (commentCreateRequest.parentId() != null) {
            parentComment = commentRepository.getById(commentCreateRequest.parentId());
            validateCommentDepth(parentComment);
        }
        Comment comment = commentCreateRequest.toEntity(siteUser, post, parentComment);
        Comment createdComment = commentRepository.save(comment);

        return CommentCreateResponse.from(createdComment);
    }

    // 대대댓글부터 허용하지 않음
    private void validateCommentDepth(Comment parentComment) {
        if (parentComment.getParentComment() != null) {
            throw new CustomException(INVALID_COMMENT_LEVEL);
        }
    }

    @Transactional
    public CommentUpdateResponse updateComment(SiteUser siteUser, Long commentId, CommentUpdateRequest commentUpdateRequest) {
        Comment comment = commentRepository.getById(commentId);
        validateDeprecated(comment);
        validateOwnership(comment, siteUser);

        comment.updateContent(commentUpdateRequest.content());

        return CommentUpdateResponse.from(comment);
    }

    private void validateDeprecated(Comment comment) {
        if (comment.getContent() == null) {
            throw new CustomException(CAN_NOT_UPDATE_DEPRECATED_COMMENT);
        }
    }

    @Transactional
    public CommentDeleteResponse deleteCommentById(SiteUser siteUser, Long commentId) {
        Comment comment = commentRepository.getById(commentId);
        validateOwnership(comment, siteUser);

        if (comment.getParentComment() != null) {
            // 대댓글인 경우
            Comment parentComment = comment.getParentComment();
            // 대댓글을 삭제합니다.
            comment.resetPostAndParentComment();
            commentRepository.deleteById(commentId);
            // 대댓글 삭제 이후, 부모댓글이 무의미하다면 이역시 삭제합니다.
            if (parentComment.getCommentList().isEmpty() && parentComment.getContent() == null) {
                parentComment.resetPostAndParentComment();
                commentRepository.deleteById(parentComment.getId());
            }
        } else {
            // 댓글인 경우
            if (comment.getCommentList().isEmpty()) {
                // 대댓글이 없는 경우
                comment.resetPostAndParentComment();
                commentRepository.deleteById(commentId);
            } else {
                // 대댓글이 있는 경우
                comment.deprecateComment();
            }
        }
        return new CommentDeleteResponse(commentId);
    }

    private void validateOwnership(Comment comment, SiteUser siteUser) {
        if (!Objects.equals(comment.getSiteUserId(), siteUser.getId())) {
            throw new CustomException(INVALID_POST_ACCESS);
        }
    }
}
