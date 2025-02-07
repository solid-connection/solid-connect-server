package com.example.solidconnection.community.post.repository;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.community.post.domain.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_ID;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"postImageList", "board", "siteUser"})
    Optional<Post> findPostById(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                UPDATE Post p SET p.likeCount = p.likeCount - 1
                WHERE p.id = :postId AND p.likeCount > 0
            """)
    void decreaseLikeCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                UPDATE Post p SET p.likeCount = p.likeCount + 1
                WHERE p.id = :postId
            """)
    void increaseLikeCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                UPDATE Post p SET p.viewCount = p.viewCount + :count
                WHERE p.id = :postId
            """)
    void increaseViewCount(@Param("postId") Long postId, @Param("count") Long count);

    default Post getByIdUsingEntityGraph(Long id) {
        return findPostById(id)
                .orElseThrow(() -> new CustomException(INVALID_POST_ID));
    }

    default Post getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(INVALID_POST_ID));
    }
}
