package com.example.solidconnection.university.fixture;

import com.example.solidconnection.university.domain.HomeUniversity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class HomeUniversityFixture {

    private final HomeUniversityFixtureBuilder homeUniversityFixtureBuilder;

    public HomeUniversity 인하대학교() {
        return homeUniversityFixtureBuilder.homeUniversity()
                .name("인하대학교")
                .create();
    }
}
