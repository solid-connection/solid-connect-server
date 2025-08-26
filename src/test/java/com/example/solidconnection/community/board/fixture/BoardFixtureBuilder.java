package com.example.solidconnection.community.board.fixture;

import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.community.board.repository.BoardRepositoryForTest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class BoardFixtureBuilder {

    private final BoardRepositoryForTest boardRepositoryForTest;

    private String code;
    private String koreanName;

    public BoardFixtureBuilder code(String code) {
        this.code = code;
        return this;
    }

    public BoardFixtureBuilder koreanName(String koreanName) {
        this.koreanName = koreanName;
        return this;
    }

    public Board findOrCreate() {
        return boardRepositoryForTest.findByCode(code)
                .orElseGet(() -> boardRepositoryForTest.save(new Board(code, koreanName)));
    }
}
