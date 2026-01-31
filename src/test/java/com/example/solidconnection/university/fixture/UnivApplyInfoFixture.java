package com.example.solidconnection.university.fixture;

import com.example.solidconnection.university.domain.UnivApplyInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class UnivApplyInfoFixture {

    private final UnivApplyInfoFixtureBuilder univApplyInfoFixtureBuilder;
    private final UniversityFixture universityFixture;
    private final HomeUniversityFixture homeUniversityFixture;

    public UnivApplyInfo 괌대학_A_지원_정보(long termId) {
        return univApplyInfoFixtureBuilder.univApplyInfo()
                .termId(termId)
                .koreanName("괌대학(A형)")
                .university(universityFixture.괌_대학())
                .homeUniversity(homeUniversityFixture.인하대학교())
                .create();
    }

    public UnivApplyInfo 괌대학_B_지원_정보(long termId) {
        return univApplyInfoFixtureBuilder.univApplyInfo()
                .termId(termId)
                .koreanName("괌대학(B형)")
                .university(universityFixture.괌_대학())
                .homeUniversity(homeUniversityFixture.인하대학교())
                .create();
    }

    public UnivApplyInfo 네바다주립대학_라스베이거스_지원_정보(long termId) {
        return univApplyInfoFixtureBuilder.univApplyInfo()
                .termId(termId)
                .koreanName("네바다주립대학 라스베이거스(B형)")
                .university(universityFixture.네바다주립_대학_라스베이거스())
                .homeUniversity(homeUniversityFixture.인하대학교())
                .create();
    }

    public UnivApplyInfo 아칸소주립대학_지원_정보(long termId) {
        return univApplyInfoFixtureBuilder.univApplyInfo()
                .termId(termId)
                .koreanName("아칸소 주립 대학")
                .university(universityFixture.아칸소_주립_대학())
                .homeUniversity(homeUniversityFixture.인하대학교())
                .create();
    }

    public UnivApplyInfo 메모리얼대학_세인트존스_A_지원_정보(long termId) {
        return univApplyInfoFixtureBuilder.univApplyInfo()
                .termId(termId)
                .koreanName("메모리얼 대학 세인트존스(A형)")
                .university(universityFixture.메모리얼_대학_세인트존스())
                .homeUniversity(homeUniversityFixture.인하대학교())
                .create();
    }

    public UnivApplyInfo 서던덴마크대학교_지원_정보(long termId) {
        return univApplyInfoFixtureBuilder.univApplyInfo()
                .termId(termId)
                .koreanName("서던덴마크대학교")
                .university(universityFixture.서던덴마크_대학())
                .homeUniversity(homeUniversityFixture.인하대학교())
                .create();
    }

    public UnivApplyInfo 코펜하겐IT대학_지원_정보(long termId) {
        return univApplyInfoFixtureBuilder.univApplyInfo()
                .termId(termId)
                .koreanName("코펜하겐 IT대학")
                .university(universityFixture.코펜하겐IT_대학())
                .homeUniversity(homeUniversityFixture.인하대학교())
                .create();
    }

    public UnivApplyInfo 그라츠대학_지원_정보(long termId) {
        return univApplyInfoFixtureBuilder.univApplyInfo()
                .termId(termId)
                .koreanName("그라츠 대학")
                .university(universityFixture.그라츠_대학())
                .homeUniversity(homeUniversityFixture.인하대학교())
                .create();
    }

    public UnivApplyInfo 그라츠공과대학_지원_정보(long termId) {
        return univApplyInfoFixtureBuilder.univApplyInfo()
                .termId(termId)
                .koreanName("그라츠공과대학")
                .university(universityFixture.그라츠공과_대학())
                .homeUniversity(homeUniversityFixture.인하대학교())
                .create();
    }

    public UnivApplyInfo 린츠_카톨릭대학_지원_정보(long termId) {
        return univApplyInfoFixtureBuilder.univApplyInfo()
                .termId(termId)
                .koreanName("린츠 카톨릭 대학교")
                .university(universityFixture.린츠_카톨릭_대학())
                .homeUniversity(homeUniversityFixture.인하대학교())
                .create();
    }

    public UnivApplyInfo 메이지대학_지원_정보(long termId) {
        return univApplyInfoFixtureBuilder.univApplyInfo()
                .termId(termId)
                .koreanName("메이지대학")
                .university(universityFixture.메이지_대학())
                .homeUniversity(homeUniversityFixture.인하대학교())
                .create();
    }
}
