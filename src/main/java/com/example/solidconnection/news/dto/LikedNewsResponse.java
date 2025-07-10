package com.example.solidconnection.news.dto;

public record LikedNewsResponse(
        boolean isLike
) {

    public static LikedNewsResponse of(boolean isLike) {
        return new LikedNewsResponse(isLike);
    }
}
