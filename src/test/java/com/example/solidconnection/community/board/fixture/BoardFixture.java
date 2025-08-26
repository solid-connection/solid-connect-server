package com.example.solidconnection.community.board.fixture;

import static com.example.solidconnection.community.board.domain.BoardCode.AMERICAS;
import static com.example.solidconnection.community.board.domain.BoardCode.ASIA;
import static com.example.solidconnection.community.board.domain.BoardCode.EUROPE;
import static com.example.solidconnection.community.board.domain.BoardCode.FREE;

import com.example.solidconnection.community.board.domain.Board;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class BoardFixture {

    private final BoardFixtureBuilder boardFixtureBuilder;

    public Board 미주권() {
        return boardFixtureBuilder.code(AMERICAS.name())
                .koreanName("미주권")
                .findOrCreate();
    }

    public Board 아시아권() {
        return boardFixtureBuilder.code(ASIA.name())
                .koreanName("아시아권")
                .findOrCreate();
    }

    public Board 유럽권() {
        return boardFixtureBuilder.code(EUROPE.name())
                .koreanName("유럽권")
                .findOrCreate();
    }

    public Board 자유게시판() {
        return boardFixtureBuilder.code(FREE.name())
                .koreanName("자유게시판")
                .findOrCreate();
    }
}
