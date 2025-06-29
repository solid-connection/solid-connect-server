package com.example.solidconnection.news.service;

import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.NewsItemResponse;
import com.example.solidconnection.news.dto.NewsResponse;
import com.example.solidconnection.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsQueryService {

    private final NewsRepository newsRepository;

    @Transactional(readOnly = true)
    public NewsResponse findNewsBySiteUserId(Long siteUserId) {
        List<News> newsList = newsRepository.findAllBySiteUserIdOrderByUpdatedAtDesc(siteUserId);
        List<NewsItemResponse> newsItemsResponseList = newsList.stream()
                .map(NewsItemResponse::from)
                .toList();
        return NewsResponse.from(newsItemsResponseList);
    }
}
