package com.example.solidconnection.news.dto;

import com.example.solidconnection.news.domain.News;

public record NewsCommandResponse(
        long id
) {

    public static NewsCommandResponse from(News news) {
        return new NewsCommandResponse(
                news.getId()
        );
    }
}
