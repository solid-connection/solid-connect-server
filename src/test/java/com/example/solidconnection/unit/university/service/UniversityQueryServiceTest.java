package com.example.solidconnection.unit.university.service;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.entity.Country;
import com.example.solidconnection.entity.Region;
import com.example.solidconnection.siteuser.repository.LikedUniversityRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.type.SemesterAvailableForDispatch;
import com.example.solidconnection.type.TuitionFeeType;
import com.example.solidconnection.university.domain.LanguageRequirement;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.dto.UniversityDetailResponse;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import com.example.solidconnection.university.repository.custom.UniversityFilterRepositoryImpl;
import com.example.solidconnection.university.service.UniversityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("대학교 조회 서비스 테스트")
class UniversityQueryServiceTest {

    @InjectMocks
    private UniversityService universityService;

    @Mock
    private UniversityFilterRepositoryImpl universityFilterRepository;

    @Mock
    private UniversityInfoForApplyRepository universityInfoForApplyRepository;

    @Mock
    private LikedUniversityRepository likedUniversityRepository;

    @Mock
    private SiteUserRepository siteUserRepository;

    private UniversityInfoForApply testUniversityInfoForApply;
    @BeforeEach
    void setUp() {
        universityService = new UniversityService(
                universityInfoForApplyRepository,
                likedUniversityRepository,
                universityFilterRepository,
                siteUserRepository
        );
        ReflectionTestUtils.setField(universityService, "term", "2025-1-a");
        University testUniversity = createTestUniversity();
        testUniversityInfoForApply = createTestUniversityInfoForApply(testUniversity);
    }


    @Test
    @DisplayName("유효한 ID로 상세 조회하면 대학 정보를 반환한다")
    void shouldReturnUniversityDetailsWhenValidIdProvided() {
        // given
        Long universityInfoForApplyId = testUniversityInfoForApply.getId();
        when(universityInfoForApplyRepository.getUniversityInfoForApplyById(universityInfoForApplyId))
                .thenReturn(testUniversityInfoForApply);

        // when
        UniversityDetailResponse response = universityService.getUniversityDetail(universityInfoForApplyId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(universityInfoForApplyId);
        assertThat(response.koreanName()).isEqualTo(testUniversityInfoForApply.getKoreanName());

        // verify
        verify(universityInfoForApplyRepository).getUniversityInfoForApplyById(universityInfoForApplyId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 예외를 반환한다")
    void shouldThrowExceptionWhenInvalidIdProvided() {
        // given
        Long invalidId = 999L;
        when(universityInfoForApplyRepository.getUniversityInfoForApplyById(invalidId))
                .thenThrow(new CustomException(ErrorCode.UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> universityService.getUniversityDetail(invalidId));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND.getCode());

        // verify
        verify(universityInfoForApplyRepository).getUniversityInfoForApplyById(invalidId);
    }

    @Test
    @DisplayName("검색 조건이 없는 경우 전체 대학 목록을 반환한다")
    void shouldReturnAllUniversitiesWhenNoSearchConditionProvided() {
        // given
        List<UniversityInfoForApply> universityList = List.of(testUniversityInfoForApply);
        when(universityFilterRepository.findByRegionCodeAndKeywordsAndLanguageTestTypeAndTestScoreAndTerm(
                "", List.of(), null, "", "2025-1-a"))
                .thenReturn(universityList);

        // when
        List<UniversityInfoForApplyPreviewResponse> response = universityService
                .searchUniversity("", List.of(), null, "")
                .universityInfoForApplyPreviewResponses();

        // then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(testUniversityInfoForApply.getId());

        // verify
        verify(universityFilterRepository).findByRegionCodeAndKeywordsAndLanguageTestTypeAndTestScoreAndTerm(
                "", List.of(), null, "", "2025-1-a");
    }

    @Test
    @DisplayName("검색 조건(region, keyword)을 만족하는 대학 목록을 반환한다")
    void shouldReturnFilteredUniversitiesWhenSearchConditionProvided() {
        // given
        String region = "ASIA";
        List<String> keywords = List.of("서울", "대학");
        List<UniversityInfoForApply> universityList = List.of(testUniversityInfoForApply);

        when(universityFilterRepository.findByRegionCodeAndKeywordsAndLanguageTestTypeAndTestScoreAndTerm(
                region, keywords, null, "", "2025-1-a"))
                .thenReturn(universityList);

        // when
        List<UniversityInfoForApplyPreviewResponse> response = universityService
                .searchUniversity(region, keywords, null, "")
                .universityInfoForApplyPreviewResponses();

        // then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).koreanName()).isEqualTo(testUniversityInfoForApply.getKoreanName());

        // verify
        verify(universityFilterRepository).findByRegionCodeAndKeywordsAndLanguageTestTypeAndTestScoreAndTerm(
                region, keywords, null, "", "2025-1-a");
    }


    @Test
    @DisplayName("검색 조건(region, keyword, testType, testScore)을 만족하는 대학 목록을 반환한다")
    void shouldReturnFilteredUniversitiesWhenFullSearchConditionProvided() {
        // given
        String region = "ASIA";
        List<String> keywords = List.of("서울", "대학");
        LanguageTestType testType = LanguageTestType.TOEFL_IBT;
        String testScore = "90";
        List<UniversityInfoForApply> universityList = List.of(testUniversityInfoForApply);

        when(universityFilterRepository.findByRegionCodeAndKeywordsAndLanguageTestTypeAndTestScoreAndTerm(
                region, keywords, testType, testScore, "2025-1-a"))
                .thenReturn(universityList);

        // when
        List<UniversityInfoForApplyPreviewResponse> response = universityService
                .searchUniversity(region, keywords, testType, testScore)
                .universityInfoForApplyPreviewResponses();

        // then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).languageRequirements()).isNotEmpty();

        // verify
        verify(universityFilterRepository).findByRegionCodeAndKeywordsAndLanguageTestTypeAndTestScoreAndTerm(
                region, keywords, testType, testScore, "2025-1-a");
    }

    @Test
    @DisplayName("검색 조건을 만족하지 않을 경우 결과가 비어 있다")
    void shouldReturnEmptyListWhenSearchConditionNotMet() {
        // given
        String region = "EUROPE";
        List<String> keywords = List.of("비현실적인 대학명");
        LanguageTestType testType = LanguageTestType.TOEFL_IBT;
        String testScore = "150";

        when(universityFilterRepository.findByRegionCodeAndKeywordsAndLanguageTestTypeAndTestScoreAndTerm(
                region, keywords, testType, testScore, "2025-1-a"))
                .thenReturn(List.of());

        // when
        List<UniversityInfoForApplyPreviewResponse> response = universityService
                .searchUniversity(region, keywords, testType, testScore)
                .universityInfoForApplyPreviewResponses();

        // then
        assertThat(response).isNotNull();
        assertThat(response).isEmpty();

        // verify
        verify(universityFilterRepository).findByRegionCodeAndKeywordsAndLanguageTestTypeAndTestScoreAndTerm(
                region, keywords, testType, testScore, "2025-1-a");
    }
    private University createTestUniversity() {
        Region region = new Region("ASIA", "아시아권");
        Country country = new Country("KR", "대한민국", region);
        return new University(
                1L,
                "서울대학교",
                "Seoul National University",
                "SNU",
                "https://www.snu.ac.kr",
                "https://english.snu.ac.kr",
                "https://accommodation.snu.ac.kr",
                "https://logo.snu.ac.kr",
                "https://background.snu.ac.kr",
                "서울에 위치한 최고의 대학",
                country,
                region
        );
    }

    private UniversityInfoForApply createTestUniversityInfoForApply(University university) {
        LanguageRequirement languageRequirement = new LanguageRequirement(
                1L,
                LanguageTestType.TOEFL_IBT,
                "90",
                null
        );
        return new UniversityInfoForApply(
                1L,
                "2025-1-a",
                "서울대학교",
                100,
                TuitionFeeType.HOME_UNIVERSITY_PAYMENT,
                SemesterAvailableForDispatch.IRRELEVANT,
                "4학기 이상",
                "TOEFL iBT 90 이상",
                "3.0/4.5",
                "4.5",
                "지원 상세 정보",
                "전공 상세 정보",
                "기숙사 상세 정보",
                "영어 강좌 상세 정보",
                "기타 상세 정보",
                new HashSet<>(List.of(languageRequirement)),
                university
        );
    }
}