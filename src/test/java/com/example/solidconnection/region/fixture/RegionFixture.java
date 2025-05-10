package com.example.solidconnection.region.fixture;

import com.example.solidconnection.entity.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class RegionFixture {

    private final RegionFixtureBuilder regionFixtureBuilder;

    public Region 영미권() {
        return regionFixtureBuilder.region()
                .code("AMERICAS")
                .koreanName("영미권")
                .findOrCreate();
    }

    public Region 유럽() {
        return regionFixtureBuilder.region()
                .code("EUROPE")
                .koreanName("유럽")
                .findOrCreate();
    }

    public Region 아시아() {
        return regionFixtureBuilder.region()
                .code("ASIA")
                .koreanName("아시아")
                .findOrCreate();
    }
}
