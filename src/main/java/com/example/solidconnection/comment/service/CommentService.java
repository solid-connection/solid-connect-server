package com.example.solidconnection.comment.service;

import com.example.solidconnection.comment.dto.*;
import com.example.solidconnection.comment.repository.CommentRepository;
import com.example.solidconnection.comment.domain.Comment;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.repository.PostRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.solidconnection.custom.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final SiteUserRepository siteUserRepository;
    private final PostRepository postRepository;

    private Boolean isOwner(Comment comment, String email) {
        return comment.getSiteUser().getEmail().equals(email);
    }

    private void validateOwnership(Comment comment, String email) {
        if (!comment.getSiteUser().getEmail().equals(email)) {
            throw new CustomException(INVALID_POST_ACCESS);
        }
    }

    private void validateDeprecated(Comment comment) {
        if (comment.getContent() == null) {
            throw new CustomException(CAN_NOT_UPDATE_DEPRECATED_COMMENT);
        }
    }

    // 대대댓글부터 허용하지 않음
    private void validateCommentDepth(Comment parentComment) {
        if (parentComment.getParentComment() != null) {
            throw new CustomException(INVALID_COMMENT_LEVEL);
        }
    }

    @Transactional(readOnly = true)
    public List<PostFindCommentResponse> findCommentsByPostId(String email, Long postId) {
        return commentRepository.findCommentTreeByPostId(postId)
                .stream()
                .map(comment -> PostFindCommentResponse.from(isOwner(comment, email), comment))
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentCreateResponse createComment(String email, Long postId, CommentCreateRequest commentCreateRequest) {

        SiteUser siteUser = siteUserRepository.getByEmail(email);
        Post post = postRepository.getById(postId);

        Comment parentComment = null;
        if (commentCreateRequest.parentId() != null) {
            parentComment = commentRepository.getById(commentCreateRequest.parentId());
            validateCommentDepth(parentComment);
        }
        Comment createdComment = commentRepository.save(commentCreateRequest.toEntity(siteUser, post, parentComment));

        return CommentCreateResponse.from(createdComment);
    }

    @Transactional
    public CommentUpdateResponse updateComment(String email, Long postId, Long commentId, CommentUpdateRequest commentUpdateRequest) {

        SiteUser siteUser = siteUserRepository.getByEmail(email);
        Post post = postRepository.getById(postId);
        Comment comment = commentRepository.getById(commentId);
        validateDeprecated(comment);
        validateOwnership(comment, email);

        comment.updateContent(commentUpdateRequest.content());

        return CommentUpdateResponse.from(comment);
    }

    @Transactional
    public CommentDeleteResponse deleteCommentById(String email, Long postId, Long commentId) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        Post post = postRepository.getById(postId);
        Comment comment = commentRepository.getById(commentId);
        validateOwnership(comment, email);

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
}
