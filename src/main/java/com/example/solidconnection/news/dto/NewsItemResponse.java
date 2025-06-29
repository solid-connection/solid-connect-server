package com.example.solidconnection.news.dto;

import com.example.solidconnection.news.domain.News;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public record NewsItemResponse(
        long id,
        String title,

        @JsonProperty("contentPreview")
        String description,

        String thumbnailUrl,
        String url,
        ZonedDateTime updatedAt
) {
    public static NewsItemResponse from(News news) {
        return new NewsItemResponse(
                news.getId(),
                news.getTitle(),
                news.getDescription(),
                news.getThumbnailUrl(),
                news.getUrl(),
                news.getUpdatedAt()
        );
    }
}
