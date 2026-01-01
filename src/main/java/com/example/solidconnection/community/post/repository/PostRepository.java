package com.example.solidconnection.community.post.repository;

import static com.example.solidconnection.common.exception.ErrorCode.INVALID_POST_ID;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.community.post.domain.Post;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByBoardCodeOrderByCreatedAtDesc(String boardCode);

    @Query("""
           SELECT p FROM Post p
           WHERE p.boardCode = :boardCode
           AND p.siteUserId NOT IN (
               SELECT ub.blockedId FROM UserBlock ub WHERE ub.blockerId = :siteUserId
           )
           ORDER BY p.createdAt DESC
           """)
    List<Post> findByBoardCodeExcludingBlockedUsersOrderByCreatedAtDesc(@Param("boardCode") String boardCode, @Param("siteUserId") Long siteUserId);

    @EntityGraph(attributePaths = {"postImageList"})
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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
               UPDATE post p SET p.is_deleted = :isDeleted
               WHERE p.site_user_id = :siteUserId
               AND p.id IN (SELECT r.target_id FROM report r WHERE r.target_type = 'POST')
           """, nativeQuery = true)
    void updateReportedPostsIsDeleted(@Param("siteUserId") long siteUserId, @Param("isDeleted") boolean isDeleted);

    default Post getByIdUsingEntityGraph(Long id) {
        return findPostById(id)
                .orElseThrow(() -> new CustomException(INVALID_POST_ID));
    }

    default Post getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(INVALID_POST_ID));
    }
}
