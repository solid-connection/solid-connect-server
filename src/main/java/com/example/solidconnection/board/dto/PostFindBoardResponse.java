package com.example.solidconnection.board.dto;

import com.example.solidconnection.board.domain.Board;

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
