package com.example.solidconnection.region.fixture;

import com.example.solidconnection.location.region.domain.Region;
import com.example.solidconnection.region.repository.RegionRepositoryForTest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class RegionFixtureBuilder {

    private final RegionRepositoryForTest regionRepositoryForTest;

    private String code;
    private String koreanName;

    public RegionFixtureBuilder region() {
        return new RegionFixtureBuilder(regionRepositoryForTest);
    }

    public RegionFixtureBuilder code(String code) {
        this.code = code;
        return this;
    }

    public RegionFixtureBuilder koreanName(String koreanName) {
        this.koreanName = koreanName;
        return this;
    }

    public Region findOrCreate() {
        return regionRepositoryForTest.findByCode(code)
                .orElseGet(() -> regionRepositoryForTest.save(new Region(code, koreanName)));
    }
}
