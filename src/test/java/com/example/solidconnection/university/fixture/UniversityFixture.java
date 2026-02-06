package com.example.solidconnection.university.fixture;

import com.example.solidconnection.location.country.fixture.CountryFixture;
import com.example.solidconnection.location.region.fixture.RegionFixture;
import com.example.solidconnection.university.domain.HostUniversity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public final class UniversityFixture {

    private final RegionFixture regionFixture;
    private final CountryFixture countryFixture;
    private final UniversityFixtureBuilder universityFixtureBuilder;

    public HostUniversity 괌_대학() {
        return universityFixtureBuilder.university()
                .koreanName("괌 대학")
                .englishName("University of Guam")
                .country(countryFixture.미국())
                .region(regionFixture.영미권())
                .findOrCreate();
    }

    public HostUniversity 네바다주립_대학_라스베이거스() {
        return universityFixtureBuilder.university()
                .koreanName("네바다주립 대학 라스베이거스")
                .englishName("University of Nevada, Las Vegas")
                .country(countryFixture.미국())
                .region(regionFixture.영미권())
                .findOrCreate();
    }

    public HostUniversity 아칸소_주립_대학() {
        return universityFixtureBuilder.university()
                .koreanName("아칸소 주립 대학")
                .englishName("Arkansas State University")
                .country(countryFixture.미국())
                .region(regionFixture.영미권())
                .findOrCreate();
    }

    public HostUniversity 메모리얼_대학_세인트존스() {
        return universityFixtureBuilder.university()
                .koreanName("메모리얼 대학 세인트존스")
                .englishName("Memorial University of Newfoundland St. John's")
                .country(countryFixture.캐나다())
                .region(regionFixture.영미권())
                .findOrCreate();
    }

    public HostUniversity 서던덴마크_대학() {
        return universityFixtureBuilder.university()
                .koreanName("서던덴마크 대학")
                .englishName("University of Southern Denmark")
                .country(countryFixture.덴마크())
                .region(regionFixture.유럽())
                .findOrCreate();
    }

    public HostUniversity 코펜하겐IT_대학() {
        return universityFixtureBuilder.university()
                .koreanName("코펜하겐IT 대학")
                .englishName("IT University of Copenhagen")
                .country(countryFixture.덴마크())
                .region(regionFixture.유럽())
                .findOrCreate();
    }

    public HostUniversity 그라츠_대학() {
        return universityFixtureBuilder.university()
                .koreanName("그라츠 대학")
                .englishName("University of Graz")
                .country(countryFixture.오스트리아())
                .region(regionFixture.유럽())
                .findOrCreate();
    }

    public HostUniversity 그라츠공과_대학() {
        return universityFixtureBuilder.university()
                .koreanName("그라츠공과 대학")
                .englishName("Graz University of Technology")
                .country(countryFixture.오스트리아())
                .region(regionFixture.유럽())
                .findOrCreate();
    }

    public HostUniversity 린츠_카톨릭_대학() {
        return universityFixtureBuilder.university()
                .koreanName("린츠 카톨릭 대학")
                .englishName("Catholic Private University Linz")
                .country(countryFixture.오스트리아())
                .region(regionFixture.유럽())
                .findOrCreate();
    }

    public HostUniversity 메이지_대학() {
        return universityFixtureBuilder.university()
                .koreanName("메이지 대학")
                .englishName("Meiji University")
                .country(countryFixture.일본())
                .region(regionFixture.아시아())
                .findOrCreate();
    }
}
