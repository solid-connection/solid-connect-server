package com.example.solidconnection.community.dto.board;

import com.example.solidconnection.community.domain.board.Board;

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
