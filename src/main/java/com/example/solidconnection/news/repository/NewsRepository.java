package com.example.solidconnection.news.repository;

import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.repository.custom.NewsCustomRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long>, NewsCustomRepository {

    List<News> findAllByOrderByUpdatedAtDesc();

    List<News> findAllBySiteUserIdOrderByUpdatedAtDesc(long siteUserId);

    void deleteAllBySiteUserId(long siteUserId);
}
