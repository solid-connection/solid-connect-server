package com.example.solidconnection.community.post.service;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostLike;
import com.example.solidconnection.community.post.dto.PostDislikeResponse;
import com.example.solidconnection.community.post.dto.PostLikeResponse;
import com.example.solidconnection.community.post.repository.PostLikeRepository;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.BoardCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static com.example.solidconnection.custom.exception.ErrorCode.DUPLICATE_POST_LIKE;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_BOARD_CODE;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PostLikeResponse likePost(SiteUser siteUser, String code, Long postId) {
        String boardCode = validateCode(code);
        Post post = postRepository.getById(postId);
        validateDuplicatePostLike(post, siteUser);

        PostLike postLike = new PostLike();
        postLike.setPostAndSiteUser(post, siteUser);
        postLikeRepository.save(postLike);
        postRepository.increaseLikeCount(post.getId());

        return PostLikeResponse.from(postRepository.getById(postId)); // 실시간성을 위한 재조회
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PostDislikeResponse dislikePost(SiteUser siteUser, String code, Long postId) {
        String boardCode = validateCode(code);
        Post post = postRepository.getById(postId);

        PostLike postLike = postLikeRepository.getByPostAndSiteUser(post, siteUser);
        postLike.resetPostAndSiteUser();
        postLikeRepository.deleteById(postLike.getId());
        postRepository.decreaseLikeCount(post.getId());

        return PostDislikeResponse.from(postRepository.getById(postId)); // 실시간성을 위한 재조회
    }

    private String validateCode(String code) {
        try {
            return String.valueOf(BoardCode.valueOf(code));
        } catch (IllegalArgumentException ex) {
            throw new CustomException(INVALID_BOARD_CODE);
        }
    }

    private void validateDuplicatePostLike(Post post, SiteUser siteUser) {
        if (postLikeRepository.findPostLikeByPostAndSiteUser(post, siteUser).isPresent()) {
            throw new CustomException(DUPLICATE_POST_LIKE);
        }
    }
}
