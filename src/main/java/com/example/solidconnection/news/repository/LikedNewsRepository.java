package com.example.solidconnection.news.repository;

import com.example.solidconnection.news.domain.LikedNews;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikedNewsRepository extends JpaRepository<LikedNews, Long> {

    boolean existsByNewsIdAndSiteUserId(long newsId, long siteUserId);
}
