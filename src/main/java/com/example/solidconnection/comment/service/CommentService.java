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
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.solidconnection.custom.exception.ErrorCode.CAN_NOT_UPDATE_DEPRECATED_COMMENT;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_ACCESS;

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

        if (comment.getCommentList().isEmpty()) {
            // 하위 댓글이 없다면 삭제한다.
            comment.resetPostAndSiteUserAndParentComment();
            commentRepository.deleteById(commentId);
        } else {
            // 하위 댓글 있으면 value만 null로 수정한다.
            comment.deprecateComment();
        }

        return new CommentDeleteResponse(commentId);
    }
}
