package com.example.solidconnection.news.dto;

import java.util.List;

public record NewsResponse(
        List<NewsItemResponse> newsItemsResponseList
) {
    public static NewsResponse from(List<NewsItemResponse> newsItemsResponseList) {
        return new NewsResponse(newsItemsResponseList);
    }
}
