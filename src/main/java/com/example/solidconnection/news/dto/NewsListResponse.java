package com.example.solidconnection.news.dto;

import java.util.List;

public record NewsListResponse(
        List<NewsResponse> newsResponseList
) {

    public static NewsListResponse from(List<NewsResponse> newsResponseList) {
        return new NewsListResponse(newsResponseList);
    }
}
