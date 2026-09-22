package com.example.solidconnection.news.repository;

import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.repository.custom.NewsCustomRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewsRepository extends JpaRepository<News, Long>, NewsCustomRepository {

    List<News> findAllByOrderByUpdatedAtDesc();

    List<News> findAllBySiteUserIdOrderByUpdatedAtDesc(long siteUserId);

    @Query("SELECT n.id FROM News n WHERE n.siteUserId = :siteUserId")
    List<Long> findAllIdsBySiteUserId(@Param("siteUserId") long siteUserId);

    void deleteAllBySiteUserId(long siteUserId);
}
