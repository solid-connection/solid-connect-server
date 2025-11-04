package com.example.solidconnection.term.fixture;

import com.example.solidconnection.term.domain.Term;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class TermFixture {

    private final TermFixtureBuilder termFixtureBuilder;

    public Term 현재_학기(String name) {
        return termFixtureBuilder.term()
                .name(name)
                .isCurrent(true)
                .findOrCreate();
    }

    public Term 이전_학기(String name) {
        return termFixtureBuilder.term()
                .name(name)
                .isCurrent(false)
                .findOrCreate();
    }
}
