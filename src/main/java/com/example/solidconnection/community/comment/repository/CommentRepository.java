package com.example.solidconnection.community.comment.repository;

import static com.example.solidconnection.common.exception.ErrorCode.INVALID_COMMENT_ID;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.community.comment.domain.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query(value = """
               WITH RECURSIVE CommentTree AS (
                       SELECT
                               id, parent_id, post_id, site_user_id, content,
                               created_at, updated_at, is_deleted,
                               0 AS level, CAST(id AS CHAR(255)) AS path
                       FROM comment
                       WHERE post_id = :postId AND parent_id IS NULL
                       AND site_user_id NOT IN (
                           SELECT blocked_id FROM user_block WHERE blocker_id = :siteUserId
                       )
                       UNION ALL
                       SELECT
                               c.id, c.parent_id, c.post_id, c.site_user_id, c.content,
                               c.created_at, c.updated_at, c.is_deleted,
                               ct.level + 1, CONCAT(ct.path, '->', c.id)
                       FROM comment c
                       INNER JOIN CommentTree ct ON c.parent_id = ct.id
                       WHERE c.site_user_id NOT IN (
                           SELECT blocked_id FROM user_block WHERE blocker_id = :siteUserId
                       )
               )
               SELECT * FROM CommentTree
               ORDER BY path
               """, nativeQuery = true)
    List<Comment> findCommentTreeByPostIdExcludingBlockedUsers(@Param("postId") Long postId, @Param("siteUserId") Long siteUserId);

    default Comment getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(INVALID_COMMENT_ID));
    }
}
