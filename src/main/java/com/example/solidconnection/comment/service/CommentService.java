package com.example.solidconnection.comment.service;

import com.example.solidconnection.comment.repository.CommentRepository;
import com.example.solidconnection.comment.dto.PostFindCommentResponse;
import com.example.solidconnection.entity.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private Boolean isOwner(Comment comment, String email) {
        return comment.getSiteUser().getEmail().equals(email);
    }


    public List<PostFindCommentResponse> findCommentsByPostId(String email, Long postId) {
        return commentRepository.findCommentTreeByPostId(postId)
                .stream()
                .map(comment -> PostFindCommentResponse.from(isOwner(comment, email), comment))
                .collect(Collectors.toList());
    }
}
