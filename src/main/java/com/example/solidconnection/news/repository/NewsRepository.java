package com.example.solidconnection.news.repository;

import com.example.solidconnection.news.domain.News;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {

    List<News> findAllBySiteUserIdOrderByUpdatedAtDesc(long siteUserId);
}
