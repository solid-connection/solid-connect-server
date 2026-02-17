package com.example.solidconnection.news.service;

import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.NewsListResponse;
import com.example.solidconnection.news.dto.NewsResponse;
import com.example.solidconnection.news.repository.NewsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewsQueryService {

    private final NewsRepository newsRepository;

    @Transactional(readOnly = true)
    public NewsListResponse findNews(Long siteUserId, Long authorId) {
        if (siteUserId == null) {
            List<NewsResponse> newsResponseList = findNewsEntities(authorId).stream()
                    .map(news -> NewsResponse.of(news, null))
                    .toList();
            return NewsListResponse.from(newsResponseList);
        }

        List<NewsResponse> newsResponseList = (authorId == null)
                ? newsRepository.findAllNewsWithLikeStatus(siteUserId)
                : newsRepository.findNewsByAuthorIdWithLikeStatus(authorId, siteUserId);
        return NewsListResponse.from(newsResponseList);
    }

    private List<News> findNewsEntities(Long authorId) {
        if (authorId == null) {
            return newsRepository.findAllByOrderByUpdatedAtDesc();
        }
        return newsRepository.findAllBySiteUserIdOrderByUpdatedAtDesc(authorId);
    }
}
