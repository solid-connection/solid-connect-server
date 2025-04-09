package com.example.solidconnection.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Component
public class DatabaseCleaner {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void clear() {
        em.clear();
        truncate();
        Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection()
                .serverCommands()
                .flushDb();
    }

    private void truncate() {
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        getTruncateQueries().forEach(query -> em.createNativeQuery(query).executeUpdate());
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    @SuppressWarnings("unchecked")
    private List<String> getTruncateQueries() {
        String sql = """
                SELECT CONCAT('TRUNCATE TABLE ', TABLE_NAME, ';') AS q
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = (SELECT DATABASE())
                AND TABLE_TYPE = 'BASE TABLE'
                """;

        return em.createNativeQuery(sql).getResultList();
    }
}
