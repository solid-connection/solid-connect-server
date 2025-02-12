package com.example.solidconnection.community.comment.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.solidconnection.custom.exception.ErrorCode.CAN_NOT_UPDATE_DEPRECATED_COMMENT;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_COMMENT_LEVEL;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_ACCESS;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<PostFindCommentResponse> findCommentsByPostId(SiteUser siteUser, Long postId) {
        return commentRepository.findCommentTreeByPostId(postId)
                .stream()
                .map(comment -> PostFindCommentResponse.from(isOwner(comment, siteUser), comment))
                .collect(Collectors.toList());
    }

    private Boolean isOwner(Comment comment, SiteUser siteUser) {
        return comment.getSiteUser().getId().equals(siteUser.getId());
    }

    @Transactional
    public CommentCreateResponse createComment(SiteUser siteUser, Long postId, CommentCreateRequest commentCreateRequest) {
        Post post = postRepository.getById(postId);

        Comment parentComment = null;
        if (commentCreateRequest.parentId() != null) {
            parentComment = commentRepository.getById(commentCreateRequest.parentId());
            validateCommentDepth(parentComment);
        }
        Comment createdComment = commentRepository.save(commentCreateRequest.toEntity(siteUser, post, parentComment));

        return CommentCreateResponse.from(createdComment);
    }

    // 대대댓글부터 허용하지 않음
    private void validateCommentDepth(Comment parentComment) {
        if (parentComment.getParentComment() != null) {
            throw new CustomException(INVALID_COMMENT_LEVEL);
        }
    }

    @Transactional
    public CommentUpdateResponse updateComment(SiteUser siteUser, Long postId, Long commentId, CommentUpdateRequest commentUpdateRequest) {
        Post post = postRepository.getById(postId);
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
    public CommentDeleteResponse deleteCommentById(SiteUser siteUser, Long postId, Long commentId) {
        Post post = postRepository.getById(postId);
        Comment comment = commentRepository.getById(commentId);
        validateOwnership(comment, siteUser);

        if (comment.getParentComment() != null) {
            // 대댓글인 경우
            Comment parentComment = comment.getParentComment();
            // 대댓글을 삭제합니다.
            comment.resetPostAndSiteUserAndParentComment();
            commentRepository.deleteById(commentId);
            // 대댓글 삭제 이후, 부모댓글이 무의미하다면 이역시 삭제합니다.
            if (parentComment.getCommentList().isEmpty() && parentComment.getContent() == null) {
                parentComment.resetPostAndSiteUserAndParentComment();
                commentRepository.deleteById(parentComment.getId());
            }
        } else {
            // 댓글인 경우
            if (comment.getCommentList().isEmpty()) {
                // 대댓글이 없는 경우
                comment.resetPostAndSiteUserAndParentComment();
                commentRepository.deleteById(commentId);
            } else {
                // 대댓글이 있는 경우
                comment.deprecateComment();
            }
        }
        return new CommentDeleteResponse(commentId);
    }

    private void validateOwnership(Comment comment, SiteUser siteUser) {
        if (!comment.getSiteUser().getId().equals(siteUser.getId())) {
            throw new CustomException(INVALID_POST_ACCESS);
        }
    }
}
