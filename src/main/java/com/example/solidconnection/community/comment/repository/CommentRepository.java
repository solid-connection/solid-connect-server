package com.example.solidconnection.community.comment.repository;

import com.example.solidconnection.community.comment.domain.Comment;
import com.example.solidconnection.custom.exception.CustomException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_COMMENT_ID;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query(value = """
            WITH RECURSIVE CommentTree AS (
                    SELECT
                            id, parent_id, post_id, site_user_id, content,
                            created_at, updated_at,
                            0 AS level, CAST(id AS CHAR(255)) AS path
                    FROM comment
                    WHERE post_id = :postId AND parent_id IS NULL
                    UNION ALL
                    SELECT
                            c.id, c.parent_id, c.post_id, c.site_user_id, c.content,
                            c.created_at, c.updated_at,
                            ct.level + 1, CONCAT(ct.path, '->', c.id)
                    FROM comment c
                    INNER JOIN CommentTree ct ON c.parent_id = ct.id
            )
            SELECT * FROM CommentTree
            ORDER BY path
            """, nativeQuery = true)
    List<Comment> findCommentTreeByPostId(@Param("postId") Long postId);

    default Comment getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(INVALID_COMMENT_ID));
    }
}
