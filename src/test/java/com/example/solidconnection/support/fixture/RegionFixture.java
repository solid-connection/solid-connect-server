package com.example.solidconnection.support.fixture;

import com.example.solidconnection.entity.Region;
import com.example.solidconnection.repositories.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegionFixture {

    private final RegionRepository regionRepository;

    public RegionBuilder region() {
        return new RegionBuilder();
    }

    public class RegionBuilder {

        private String code;
        private String koreanName;

        public RegionBuilder code(String code) {
            this.code = code;
            return this;
        }

        public RegionBuilder koreanName(String koreanName) {
            this.koreanName = koreanName;
            return this;
        }

        public Region create() {
            Region region = new Region(code, koreanName);
            return regionRepository.save(region);
        }
    }
}
