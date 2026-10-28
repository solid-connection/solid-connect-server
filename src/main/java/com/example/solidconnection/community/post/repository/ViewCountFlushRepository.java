package com.example.solidconnection.community.post.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ViewCountFlushRepository {

    private final JdbcTemplate jdbcTemplate;

    public boolean recordIfNew(
            String flushId,
            long postId,
            long delta
    ) {
        try {
            jdbcTemplate.update("INSERT INTO view_count_flush (flush_id, post_id, delta) VALUES (?, ?, ?)",
                    flushId, postId, delta);
            return true;
        } catch (DuplicateKeyException exception) {
            // A JDBC duplicate does not mark the JPA transaction rollback-only.
            return false;
        }
    }
}
