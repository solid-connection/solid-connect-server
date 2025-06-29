package com.example.solidconnection.news.repository;

import com.example.solidconnection.news.domain.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findAllBySiteUserIdOrderByUpdatedAtDesc(Long siteUserId);
}
