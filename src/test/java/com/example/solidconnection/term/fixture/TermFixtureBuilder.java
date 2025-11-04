package com.example.solidconnection.term.fixture;

import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.repository.TermRepositoryForTest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class TermFixtureBuilder {

    private final TermRepositoryForTest termRepositoryForTest;

    private String name;
    private boolean isCurrent;

    public TermFixtureBuilder term() {
        return new TermFixtureBuilder(termRepositoryForTest);
    }

    public TermFixtureBuilder name(String name) {
        this.name = name;
        return this;
    }

    public TermFixtureBuilder isCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
        return this;
    }

    public Term findOrCreate() {
        return termRepositoryForTest.findByName(name)
                .orElseGet(() -> termRepositoryForTest.save(new Term(name, isCurrent)));
    }
}
