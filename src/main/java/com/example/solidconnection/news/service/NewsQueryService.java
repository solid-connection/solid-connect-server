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
    public NewsListResponse findNewsBySiteUserId(long siteUserId) {
        List<News> newsList = newsRepository.findAllBySiteUserIdOrderByUpdatedAtDesc(siteUserId);
        List<NewsResponse> newsResponseList = newsList.stream()
                .map(NewsResponse::from)
                .toList();
        return NewsListResponse.from(newsResponseList);
    }
}
