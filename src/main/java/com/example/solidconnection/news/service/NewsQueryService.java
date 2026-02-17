package com.example.solidconnection.news.service;

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
    public NewsListResponse findAllNews(Long siteUserId) {
        // 로그인하지 않은 경우
        if (siteUserId == null) {
            List<NewsResponse> newsResponseList = newsRepository.findAll().stream()
                    .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                    .map(news -> NewsResponse.of(news, null))
                    .toList();
            return NewsListResponse.from(newsResponseList);
        }

        // 로그인한 경우
        List<NewsResponse> newsResponseList = newsRepository.findAllNewsWithLikeStatus(siteUserId);
        return NewsListResponse.from(newsResponseList);
    }

    @Transactional(readOnly = true)
    public NewsListResponse findNewsByAuthorId(Long siteUserId, long authorId) {
        // 로그인하지 않은 경우
        if (siteUserId == null) {
            List<NewsResponse> newsResponseList = newsRepository.findAllBySiteUserIdOrderByUpdatedAtDesc(authorId)
                    .stream()
                    .map(news -> NewsResponse.of(news, null))
                    .toList();
            return NewsListResponse.from(newsResponseList);
        }

        // 로그인한 경우
        List<NewsResponse> newsResponseList = newsRepository.findNewsByAuthorIdWithLikeStatus(authorId, siteUserId);

        return NewsListResponse.from(newsResponseList);
    }
}
