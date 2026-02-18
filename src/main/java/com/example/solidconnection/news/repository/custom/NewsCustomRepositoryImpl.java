package com.example.solidconnection.news.repository.custom;

import com.example.solidconnection.news.dto.NewsResponse;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NewsCustomRepositoryImpl implements NewsCustomRepository {

    private final EntityManager entityManager;

    @Override
    public List<NewsResponse> findAllNewsWithLikeStatus(Long siteUserId) {
        String jpql = """
                      SELECT new com.example.solidconnection.news.dto.NewsResponse(
                          n.id,
                          n.title,
                          n.description,
                          n.thumbnailUrl,
                          n.url,
                          CASE WHEN ln.id IS NOT NULL THEN true ELSE false END,
                          n.updatedAt
                      )
                      FROM News n
                      LEFT JOIN LikedNews ln ON n.id = ln.newsId AND ln.siteUserId = :siteUserId
                      ORDER BY n.updatedAt DESC
                      """;

        return entityManager.createQuery(jpql, NewsResponse.class)
                .setParameter("siteUserId", siteUserId)
                .getResultList();
    }

    @Override
    public List<NewsResponse> findNewsByAuthorIdWithLikeStatus(long authorId, Long siteUserId) {
        String jpql = """
                      SELECT new com.example.solidconnection.news.dto.NewsResponse(
                          n.id,
                          n.title,
                          n.description,
                          n.thumbnailUrl,
                          n.url,
                          CASE WHEN ln.id IS NOT NULL THEN true ELSE false END,
                          n.updatedAt
                      )
                      FROM News n
                      LEFT JOIN LikedNews ln ON n.id = ln.newsId AND ln.siteUserId = :siteUserId
                      WHERE n.siteUserId = :authorId
                      ORDER BY n.updatedAt DESC
                      """;

        return entityManager.createQuery(jpql, NewsResponse.class)
                .setParameter("authorId", authorId)
                .setParameter("siteUserId", siteUserId)
                .getResultList();
    }
}
