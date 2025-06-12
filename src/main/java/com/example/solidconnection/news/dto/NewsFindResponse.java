package com.example.solidconnection.news.dto;

import com.example.solidconnection.news.domain.News;

import java.time.ZonedDateTime;

public record NewsFindResponse(
        long id,
        String title,
        String description,
        String thumbnailUrl,
        String url,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static NewsFindResponse from(News news) {
        return new NewsFindResponse(
                news.getId(),
                news.getTitle(),
                news.getDescription(),
                news.getThumbnailUrl(),
                news.getUrl(),
                news.getCreatedAt(),
                news.getUpdatedAt()
        );
    }
}
