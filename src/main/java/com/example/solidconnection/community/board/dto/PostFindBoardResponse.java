package com.example.solidconnection.community.board.dto;

import com.example.solidconnection.community.board.domain.Board;

public record PostFindBoardResponse(
        String code,
        String koreanName
) {
    public static PostFindBoardResponse from(Board board) {
        return new PostFindBoardResponse(
                board.getCode(),
                board.getKoreanName()
        );
    }
}
