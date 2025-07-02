package com.example.solidconnection.community.post.repository;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import static com.example.solidconnection.common.exception.ErrorCode.INVALID_POST_LIKE;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findPostLikeByPostAndSiteUserId(Post post, long siteUserId);

    default PostLike getByPostAndSiteUserId(Post post, long siteUserId) {
        return findPostLikeByPostAndSiteUserId(post, siteUserId)
                .orElseThrow(() -> new CustomException(INVALID_POST_LIKE));
    }
}
