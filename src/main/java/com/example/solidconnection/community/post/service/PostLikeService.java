package com.example.solidconnection.community.post.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostLike;
import com.example.solidconnection.community.post.dto.PostDislikeResponse;
import com.example.solidconnection.community.post.dto.PostLikeResponse;
import com.example.solidconnection.community.post.repository.PostLikeRepository;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static com.example.solidconnection.common.exception.ErrorCode.DUPLICATE_POST_LIKE;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final SiteUserRepository siteUserRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PostLikeResponse likePost(SiteUser siteUser, Long postId) {
        Post post = postRepository.getById(postId);
        validateDuplicatePostLike(post, siteUser);
        PostLike postLike = new PostLike();
        postLike.setPostAndSiteUser(post, siteUser.getId());
        postLikeRepository.save(postLike);
        postRepository.increaseLikeCount(post.getId());

        return PostLikeResponse.from(postRepository.getById(postId)); // 실시간성을 위한 재조회
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PostDislikeResponse dislikePost(SiteUser siteUser, Long postId) {
        Post post = postRepository.getById(postId);

        PostLike postLike = postLikeRepository.getByPostAndSiteUserId(post, siteUser.getId());
        postLike.resetPostAndSiteUser();
        postLikeRepository.deleteById(postLike.getId());
        postRepository.decreaseLikeCount(post.getId());

        return PostDislikeResponse.from(postRepository.getById(postId)); // 실시간성을 위한 재조회
    }

    private void validateDuplicatePostLike(Post post, SiteUser siteUser) {
        if (postLikeRepository.findPostLikeByPostAndSiteUserId(post, siteUser.getId()).isPresent()) {
            throw new CustomException(DUPLICATE_POST_LIKE);
        }
    }
}
