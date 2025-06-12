package com.example.solidconnection.news.dto;

import com.example.solidconnection.news.domain.News;

import java.time.ZonedDateTime;

public record NewsItemResponse(
        long id,
        String title,
        String thumbnailUrl,
        String url,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static NewsItemResponse from(News news) {
        return new NewsItemResponse(
                news.getId(),
                news.getTitle(),
                news.getThumbnailUrl(),
                news.getUrl(),
                news.getCreatedAt(),
                news.getUpdatedAt()
        );
    }
}
