package com.example.solidconnection.news.service;

import static com.example.solidconnection.common.exception.ErrorCode.ALREADY_LIKED_NEWS;
import static com.example.solidconnection.common.exception.ErrorCode.NEWS_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.NOT_LIKED_NEWS;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.news.domain.LikedNews;
import com.example.solidconnection.news.repository.LikedNewsRepository;
import com.example.solidconnection.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class NewsLikeService {

    private final NewsRepository newsRepository;
    private final LikedNewsRepository likedNewsRepository;

    @Transactional
    public void addNewsLike(long siteUserId, long newsId) {
        if (!newsRepository.existsById(newsId)) {
            throw new CustomException(NEWS_NOT_FOUND);
        }
        if (likedNewsRepository.existsByNewsIdAndSiteUserId(newsId, siteUserId)) {
            throw new CustomException(ALREADY_LIKED_NEWS);
        }
        LikedNews likedNews = new LikedNews(newsId, siteUserId);
        likedNewsRepository.save(likedNews);
    }

    @Transactional
    public void cancelNewsLike(long siteUserId, long newsId) {
        if (!newsRepository.existsById(newsId)) {
            throw new CustomException(NEWS_NOT_FOUND);
        }
        LikedNews likedNews = likedNewsRepository.findByNewsIdAndSiteUserId(newsId, siteUserId)
                .orElseThrow(() -> new CustomException(NOT_LIKED_NEWS));
        likedNewsRepository.delete(likedNews);
    }
}
