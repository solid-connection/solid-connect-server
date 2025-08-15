package com.example.solidconnection.news.repository;

import com.example.solidconnection.news.domain.LikedNews;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikedNewsRepository extends JpaRepository<LikedNews, Long> {

    boolean existsByNewsIdAndSiteUserId(long newsId, long siteUserId);

    Optional<LikedNews> findByNewsIdAndSiteUserId(long newsId, long siteUserId);

    @Query("""
           SELECT l.newsId
           FROM LikedNews l
           WHERE l.newsId IN :newsIds AND l.siteUserId = :siteUserId
           """)
    Set<Long> findLikedNewsIdsByNewsIdsAndSiteUserId(@Param("newsIds") List<Long> newsIds, @Param("siteUserId") Long siteUserId);
}
