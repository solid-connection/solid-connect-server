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
        Boolean isLiked,

        ZonedDateTime updatedAt
) {

    public static NewsResponse of(News news, Boolean isLiked) {
        return new NewsResponse(
                news.getId(),
                news.getTitle(),
                news.getDescription(),
                news.getThumbnailUrl(),
                news.getUrl(),
                isLiked,
                news.getUpdatedAt()
        );
    }
}
