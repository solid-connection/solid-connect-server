package com.example.solidconnection.support.fixture;

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
}
