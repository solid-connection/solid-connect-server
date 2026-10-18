package com.example.solidconnection.news.repository;

import com.example.solidconnection.news.domain.LikedNews;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikedNewsRepository extends JpaRepository<LikedNews, Long> {

    boolean existsByNewsIdAndSiteUserId(long newsId, long siteUserId);

    Optional<LikedNews> findByNewsIdAndSiteUserId(long newsId, long siteUserId);

    void deleteAllBySiteUserId(long siteUserId);

    void deleteAllByNewsIdIn(List<Long> newsIds);
}
