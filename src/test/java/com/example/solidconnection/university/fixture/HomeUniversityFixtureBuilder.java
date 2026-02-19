package com.example.solidconnection.university.fixture;

import com.example.solidconnection.university.domain.HomeUniversity;
import com.example.solidconnection.university.repository.HomeUniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class HomeUniversityFixtureBuilder {

    private final HomeUniversityRepository homeUniversityRepository;

    private String name;

    public HomeUniversityFixtureBuilder homeUniversity() {
        return new HomeUniversityFixtureBuilder(homeUniversityRepository);
    }

    public HomeUniversityFixtureBuilder name(String name) {
        this.name = name;
        return this;
    }

    public HomeUniversity create() {
        return homeUniversityRepository.findByName(name)
                .orElseGet(() -> homeUniversityRepository.save(new HomeUniversity(null, name)));
    }
}
