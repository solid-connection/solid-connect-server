package com.example.solidconnection.community.post.repository;

import static com.example.solidconnection.common.exception.ErrorCode.INVALID_POST_ID;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostCategory;
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

    // 1차 쿼리 개선(2026-09-21, 미커밋 로컬 검증용):
    // 기존에는 board 전체 게시글을 위 메서드들로 가져온 뒤 PostQueryService에서
    // Java 스트림으로 category를 필터링했다(전체를 로드하고 대부분은 버림).
    // category 조건을 SQL WHERE로 내려서 DB가 필요한 행만 반환하게 했다.
    // category가 PostCategory.전체 이면 필터링 없이 전체를 반환(기존 동작과 동일).
    @Query("""
           SELECT p FROM Post p
           WHERE p.boardCode = :boardCode
           AND (:category = com.example.solidconnection.community.post.domain.PostCategory.전체 OR p.category = :category)
           ORDER BY p.createdAt DESC
           """)
    List<Post> findByBoardCodeAndCategoryOrderByCreatedAtDesc(@Param("boardCode") String boardCode, @Param("category") PostCategory category);

    @Query("""
           SELECT p FROM Post p
           WHERE p.boardCode = :boardCode
           AND (:category = com.example.solidconnection.community.post.domain.PostCategory.전체 OR p.category = :category)
           AND p.siteUserId NOT IN (
               SELECT ub.blockedId FROM UserBlock ub WHERE ub.blockerId = :siteUserId
           )
           ORDER BY p.createdAt DESC
           """)
    List<Post> findByBoardCodeAndCategoryExcludingBlockedUsersOrderByCreatedAtDesc(
            @Param("boardCode") String boardCode, @Param("category") PostCategory category, @Param("siteUserId") Long siteUserId);

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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
                       UPDATE post p SET p.is_deleted = :isDeleted
                       WHERE p.site_user_id IN :siteUserIds
                       AND p.id IN (SELECT r.target_id FROM report r WHERE r.target_type = 'POST')
                   """, nativeQuery = true)
    void bulkUpdateReportedPostsIsDeleted(@Param("siteUserIds") List<Long> siteUserIds, @Param("isDeleted") boolean isDeleted);

    default Post getByIdUsingEntityGraph(Long id) {
        return findPostById(id)
                .orElseThrow(() -> new CustomException(INVALID_POST_ID));
    }

    default Post getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(INVALID_POST_ID));
    }

    void deleteAllBySiteUserId(long siteUserId);
}
