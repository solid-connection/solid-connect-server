package com.example.solidconnection.news.dto;

import com.example.solidconnection.news.domain.News;

public record NewsResponse(
        long id
) {
    public static NewsResponse from(News news) {
        return new NewsResponse(
                news.getId()
        );
    }
}
