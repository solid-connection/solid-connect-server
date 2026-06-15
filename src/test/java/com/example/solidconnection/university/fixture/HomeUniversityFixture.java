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
                .maxChoiceCount(3)
                .create();
    }

    public HomeUniversity 최대_2지망_협정대학교() {
        return homeUniversityFixtureBuilder.homeUniversity()
                .name("테스트협정대학교_최대2지망")
                .maxChoiceCount(2)
                .create();
    }

    public HomeUniversity 인천대학교() {
        return homeUniversityFixtureBuilder.homeUniversity()
                .name("인천대학교")
                .maxChoiceCount(3)
                .create();
    }
}
