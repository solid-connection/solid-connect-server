package com.example.solidconnection.news.dto;

public record LikedNewsResponse(
        long id,
        boolean isLike
) {

    public static LikedNewsResponse of(long id, boolean isLike) {
        return new LikedNewsResponse(id, isLike);
    }
}
