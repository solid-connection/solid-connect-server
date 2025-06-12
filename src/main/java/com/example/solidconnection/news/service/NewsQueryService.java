package com.example.solidconnection.news.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.NewsFindResponse;
import com.example.solidconnection.news.dto.NewsItemResponse;
import com.example.solidconnection.news.dto.NewsResponse;
import com.example.solidconnection.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.solidconnection.common.exception.ErrorCode.NEWS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class NewsQueryService {

    private final NewsRepository newsRepository;

    @Transactional(readOnly = true)
    public NewsResponse searchNews() {
        List<News> newsList = newsRepository.findAllByOrderByUpdatedAtDesc();
        List<NewsItemResponse> newsItemsResponseList = newsList.stream()
                .map(NewsItemResponse::from)
                .toList();
        return NewsResponse.from(newsItemsResponseList);
    }

    @Transactional(readOnly = true)
    public NewsFindResponse findNewsById(Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(NEWS_NOT_FOUND));
        return NewsFindResponse.from(news);
    }
}
