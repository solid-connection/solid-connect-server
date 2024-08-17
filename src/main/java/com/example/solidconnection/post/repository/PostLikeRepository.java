package com.example.solidconnection.post.repository;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.post.domain.PostLike;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.siteuser.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_LIKE;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findPostLikeByPostAndSiteUser(Post post, SiteUser siteUser);

    default PostLike getByPostAndSiteUser(Post post, SiteUser siteUser) {
        return findPostLikeByPostAndSiteUser(post, siteUser)
                .orElseThrow(() -> new CustomException(INVALID_POST_LIKE));
    }
}
