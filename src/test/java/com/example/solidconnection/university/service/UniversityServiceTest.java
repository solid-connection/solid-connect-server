package com.example.solidconnection.university.service;

import com.example.solidconnection.cache.manager.CacheManager;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.Country;
import com.example.solidconnection.entity.Region;
import com.example.solidconnection.repositories.CountryRepository;
import com.example.solidconnection.repositories.RegionRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.LanguageRequirement;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.dto.UniversityDetailResponse;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import com.example.solidconnection.university.repository.UniversityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static com.example.solidconnection.custom.exception.ErrorCode.UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND;
import static com.example.solidconnection.type.LanguageTestType.IELTS;
import static com.example.solidconnection.type.SemesterAvailableForDispatch.IRRELEVANT;
import static com.example.solidconnection.type.TuitionFeeType.OVERSEAS_UNIVERSITY_PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;

@TestContainerSpringBootTest
@DisplayName("대학교 서비스 테스트")
class UniversityServiceTest {


    @Autowired
    private UniversityService universityService;

    @Autowired
    private UniversityInfoForApplyRepository universityInfoForApplyRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void 대학_상세정보를_정상_조회한다() {
        // given
        UniversityInfoForApply universityInfoApply = saveTestUniversityWithInfo();
        University university = universityInfoApply.getUniversity();

        // when
        UniversityDetailResponse response = universityService.getUniversityDetail(universityInfoApply.getId());

        // then
        assertThat(response.koreanName()).isEqualTo(universityInfoApply.getKoreanName());
        assertThat(response.englishName()).isEqualTo(university.getEnglishName());
        assertThat(response.term()).isEqualTo(universityInfoApply.getTerm());
        assertThat(response.region()).isEqualTo(university.getRegion().getKoreanName());
        assertThat(response.country()).isEqualTo(university.getCountry().getKoreanName());
        assertThat(response.studentCapacity()).isEqualTo(universityInfoApply.getStudentCapacity());
        assertThat(response.tuitionFeeType()).isEqualTo(universityInfoApply.getTuitionFeeType().getKoreanName());
    }

    @Test
    void 대학_상세정보_조회시_캐시가_적용된다() {
        // given
        UniversityInfoForApply savedInfo = saveTestUniversityWithInfo();
        String cacheKey = "university:" + savedInfo.getId();

        // when
        UniversityDetailResponse dbResponse = universityService.getUniversityDetail(savedInfo.getId());
        Object cachedValue = cacheManager.get(cacheKey);
        UniversityDetailResponse cacheResponse = universityService.getUniversityDetail(savedInfo.getId());

        // then
        assertThat(dbResponse).isEqualTo(cacheResponse);
        assertThat(cachedValue).isEqualTo(dbResponse);
        //verify(universityInfoForApplyRepository, times(1)).findById(savedInfo.getId());
    }


    @Test
    void 존재하지_않는_대학_상세정보_조회시_예외_응답을_반환한다() {
        // given
        Long invalidId = 9999L;

        // when &
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> universityService.getUniversityDetail(invalidId));
        CustomException customException = (CustomException)exception.getCause().getCause();

        // then
        assertThat(customException.getMessage()).isEqualTo(UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND.getMessage());
    }

    private Region createRegion() {
        return new Region("AMERICAS", "미주권");
    }

    private Country createCountry(Region region) {
        return new Country("AU", "호주", region);
    }

    private University createUniversity(Country country, Region region) {
        return new University(
                null,
                "시드니대학",
                "University of Sydney",
                "university_of_sydney",
                "https://www.sydney.edu.au/",
                "www.sydney.edu.au/sydney-abroad-units",
                "https://www.sydney.edu.au/study/accommodation.html",
                "resize/university_of_sydney/logo.webp",
                "resize/university_of_sydney/1.webp",
                "시드니 중심부에서 가까운 위치에 있는 캠퍼스는 학생들에게 활기찬 문화, 사회, 전문적인 생활을 즐길 수 있는 무한한 기회를 제공합니다. " +
                        "시드니는 시드니 오페라 하우스와 하버 브리지와 같은 상징적인 랜드마크로 유명하며, 다양한 인구와 역동적인 예술 장면으로 알려진 글로벌 도시입니다. " +
                        "University of Sydney는 다양한 분야에서 연구와 교육의 선두주자로서, 학생들에게 글로벌 관점과 사회 정의 및 지속 가능성에 대한 약속을 강화합니다.",
                country,
                region
        );
    }

    private Set<LanguageRequirement> createLanguageRequirements(UniversityInfoForApply universityInfoForApply) {
        return Set.of(
                new LanguageRequirement(null, IELTS, "6.5", universityInfoForApply),
                new LanguageRequirement(null, IELTS, "6.0", universityInfoForApply)
        );
    }

    private UniversityInfoForApply createUniversityInfoForApply(University university) {
        UniversityInfoForApply universityInfo = new UniversityInfoForApply(
                null,
                "2024-1",
                "시드니대학",
                5,
                OVERSEAS_UNIVERSITY_PAYMENT,
                IRRELEVANT,
                "2",
                "영어 점수는 다음의 세부영역 점수를 각각 만족해야함<br>" +
                        " - IELTS: 모든 영역에서 6.0 이상<br>" +
                        " - TOEFL IBT: 읽기/듣기/말하기 17점, 쓰기 19점 이상<br>" +
                        "- 어학성적은 파견학기 시작시까지 유효하여야함",
                "3.0",
                "4.0",
                null,
                "타전공 지원 및 수강 가능<br>" +
                        "- MECO, CAEL, LAWS unit 수강 여석 제한 있음",
                null,
                null,
                "OSHC(Overseas Student Health Cover) 국제학생 보험가입 의무 " +
                        "(2023년 기준 AUD 348/학기, 학기마다 비용 상이)",
                new HashSet<>(),
                university
        );

        Set<LanguageRequirement> requirements = createLanguageRequirements(universityInfo);
        requirements.forEach(universityInfo::addLanguageRequirements);
        return universityInfo;
    }

    private UniversityInfoForApply saveTestUniversityWithInfo() {
        Region region = regionRepository.save(createRegion());
        Country country = countryRepository.save(createCountry(region));

        University university = createUniversity(country, region);
        university = universityRepository.save(university);

        UniversityInfoForApply universityInfo = createUniversityInfoForApply(university);
        return universityInfoForApplyRepository.save(universityInfo);
    }
}
