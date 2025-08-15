package com.example.solidconnection.news.service;

import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.NewsListResponse;
import com.example.solidconnection.news.dto.NewsResponse;
import com.example.solidconnection.news.repository.LikedNewsRepository;
import com.example.solidconnection.news.repository.NewsRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewsQueryService {

    private final NewsRepository newsRepository;
    private final LikedNewsRepository likedNewsRepository;

    @Transactional(readOnly = true)
    public NewsListResponse findNewsByAuthorId(Long siteUserId, long authorId) {
        List<News> newsList = newsRepository.findAllBySiteUserIdOrderByUpdatedAtDesc(authorId);

        // 로그인하지 않은 경우
        if (siteUserId == null) {
            List<NewsResponse> newsResponseList = newsList.stream()
                    .map(news -> NewsResponse.from(news, null))
                    .toList();
            return NewsListResponse.from(newsResponseList);
        }

        // 로그인한 경우
        List<Long> newsIds = newsList.stream()
                .map(News::getId)
                .toList();

        Set<Long> likedNewsIds = likedNewsRepository.findLikedNewsIdsByNewsIdsAndSiteUserId(newsIds, siteUserId);
        List<NewsResponse> newsResponseList = newsList.stream()
                .map(news -> {
                    Boolean isLike = likedNewsIds.contains(news.getId());
                    return NewsResponse.from(news, isLike);
                })
                .toList();

        return NewsListResponse.from(newsResponseList);
    }
}
