package com.example.solidconnection.news.dto;

import com.example.solidconnection.news.domain.News;
import java.time.ZonedDateTime;

public record NewsResponse(
        long id,
        String title,
        String description,
        String thumbnailUrl,
        String url,
        ZonedDateTime updatedAt
) {

    public static NewsResponse from(News news) {
        return new NewsResponse(
                news.getId(),
                news.getTitle(),
                news.getDescription(),
                news.getThumbnailUrl(),
                news.getUrl(),
                news.getUpdatedAt()
        );
    }
}
