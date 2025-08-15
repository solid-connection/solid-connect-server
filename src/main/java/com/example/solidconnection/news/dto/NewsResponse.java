package com.example.solidconnection.news.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.example.solidconnection.news.domain.News;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.ZonedDateTime;

public record NewsResponse(
        long id,
        String title,
        String description,
        String thumbnailUrl,
        String url,

        @JsonInclude(NON_NULL)
        Boolean isLike,

        ZonedDateTime updatedAt
) {

    public static NewsResponse from(News news, Boolean isLike) {
        return new NewsResponse(
                news.getId(),
                news.getTitle(),
                news.getDescription(),
                news.getThumbnailUrl(),
                news.getUrl(),
                isLike,
                news.getUpdatedAt()
        );
    }
}
