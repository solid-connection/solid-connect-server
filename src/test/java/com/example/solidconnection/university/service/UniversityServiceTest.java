package com.example.solidconnection.university.service;

import com.example.solidconnection.cache.manager.CacheManager;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.university.dto.LanguageRequirementResponse;
import com.example.solidconnection.university.dto.UniversityDetailResponse;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponse;
import com.example.solidconnection.university.dto.UniversityInfoForApplyPreviewResponses;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static com.example.solidconnection.custom.exception.ErrorCode.UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("대학교 서비스 테스트")
class UniversityServiceTest extends UniversityDataSetUpIntegrationTest {


    @Autowired
    private UniversityService universityService;

    @Autowired
    private CacheManager cacheManager;

    @Test
     void 대학_상세정보를_정상_조회한다() {
        // given
        Long universityId = 괌대학_A_지원_정보.getId();

        // when
        UniversityDetailResponse response = universityService.getUniversityDetail(universityId);

        // then
        Assertions.assertAll(
                () -> assertThat(response.id()).isEqualTo(괌대학_A_지원_정보.getId()),
                () -> assertThat(response.term()).isEqualTo(괌대학_A_지원_정보.getTerm()),
                () -> assertThat(response.koreanName()).isEqualTo(괌대학_A_지원_정보.getKoreanName()),
                () -> assertThat(response.englishName()).isEqualTo(영미권_미국_괌대학.getEnglishName()),
                () -> assertThat(response.formatName()).isEqualTo(영미권_미국_괌대학.getFormatName()),
                () -> assertThat(response.region()).isEqualTo(영미권.getKoreanName()),
                () -> assertThat(response.country()).isEqualTo(미국.getKoreanName()),
                () -> assertThat(response.homepageUrl()).isEqualTo(영미권_미국_괌대학.getHomepageUrl()),
                () -> assertThat(response.logoImageUrl()).isEqualTo(영미권_미국_괌대학.getLogoImageUrl()),
                () -> assertThat(response.backgroundImageUrl()).isEqualTo(영미권_미국_괌대학.getBackgroundImageUrl()),
                () -> assertThat(response.detailsForLocal()).isEqualTo(영미권_미국_괌대학.getDetailsForLocal()),
                () -> assertThat(response.studentCapacity()).isEqualTo(괌대학_A_지원_정보.getStudentCapacity()),
                () -> assertThat(response.tuitionFeeType()).isEqualTo(괌대학_A_지원_정보.getTuitionFeeType().getKoreanName()),
                () -> assertThat(response.semesterAvailableForDispatch()).isEqualTo(괌대학_A_지원_정보.getSemesterAvailableForDispatch().getKoreanName()),
                () -> assertThat(response.languageRequirements()).containsOnlyOnceElementsOf(
                        괌대학_A_지원_정보.getLanguageRequirements().stream()
                                .map(LanguageRequirementResponse::from)
                                .toList()),
                () -> assertThat(response.detailsForLanguage()).isEqualTo(괌대학_A_지원_정보.getDetailsForLanguage()),
                () -> assertThat(response.gpaRequirement()).isEqualTo(괌대학_A_지원_정보.getGpaRequirement()),
                () -> assertThat(response.gpaRequirementCriteria()).isEqualTo(괌대학_A_지원_정보.getGpaRequirementCriteria()),
                () -> assertThat(response.semesterRequirement()).isEqualTo(괌대학_A_지원_정보.getSemesterRequirement()),
                () -> assertThat(response.detailsForApply()).isEqualTo(괌대학_A_지원_정보.getDetailsForApply()),
                () -> assertThat(response.detailsForMajor()).isEqualTo(괌대학_A_지원_정보.getDetailsForMajor()),
                () -> assertThat(response.detailsForAccommodation()).isEqualTo(괌대학_A_지원_정보.getDetailsForAccommodation()),
                () -> assertThat(response.detailsForEnglishCourse()).isEqualTo(괌대학_A_지원_정보.getDetailsForEnglishCourse()),
                () -> assertThat(response.details()).isEqualTo(괌대학_A_지원_정보.getDetails()),
                () -> assertThat(response.accommodationUrl()).isEqualTo(괌대학_A_지원_정보.getUniversity().getAccommodationUrl()),
                () -> assertThat(response.englishCourseUrl()).isEqualTo(괌대학_A_지원_정보.getUniversity().getEnglishCourseUrl())
        );
    }
    @Test
    void 대학_상세정보_조회시_캐시가_적용된다() {
        // given
        Long universityId = 괌대학_A_지원_정보.getId();
        String cacheKey = "university:" + 괌대학_A_지원_정보.getId();

        // when
        UniversityDetailResponse dbResponse = universityService.getUniversityDetail(universityId);
        Object cachedValue = cacheManager.get(cacheKey);
        UniversityDetailResponse cacheResponse = universityService.getUniversityDetail(universityId);

        // then(정말 한 번만 호출하는지 검증하는 로직이 필요한데 아직 구현이 안되었습니다.)
        assertThat(dbResponse).isEqualTo(cacheResponse);
        assertThat(cachedValue).isEqualTo(dbResponse);
    }

    @Test
    void 존재하지_않는_대학_상세정보_조회시_예외_응답을_반환한다() {
        // given
        Long invalidId = 9999L;

        // when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> universityService.getUniversityDetail(invalidId));
        CustomException customException = (CustomException) exception.getCause().getCause();

        // then
        assertThat(customException.getMessage()).isEqualTo(UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND.getMessage());
    }

    @Test
    void 전체_대학을_조회한다() {
        // when
        UniversityInfoForApplyPreviewResponses response = universityService.searchUniversity(
                null, List.of(), null, null);

        // then
        assertThat(response.universityInfoForApplyPreviewResponses())
                .containsExactlyInAnyOrder(
                        UniversityInfoForApplyPreviewResponse.from(괌대학_A_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(괌대학_B_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(네바다주립대학_라스베이거스_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(메모리얼대학_세인트존스_A_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(서던덴마크대학교_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(코펜하겐IT대학_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(그라츠대학_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(그라츠공과대학_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(린츠_카톨릭대학_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(메이지대학_지원_정보)
                );
    }

    @Test
    void 대학_조회시_캐시가_적용된다() {
        // given
        String regionCode = 영미권.getCode();
        List<String> keywords = List.of("괌");
        LanguageTestType testType = LanguageTestType.TOEFL_IBT;
        String testScore = "70";
        String cacheKey = String.format("university:%s:%s:%s:%s", regionCode, keywords, testType, testScore);

        // when
        UniversityInfoForApplyPreviewResponses firstResponse =
                universityService.searchUniversity(regionCode, keywords, testType, testScore);
        Object cachedValue = cacheManager.get(cacheKey);
        UniversityInfoForApplyPreviewResponses secondResponse =
                universityService.searchUniversity(regionCode, keywords, testType, testScore);

        // then
        assertThat(firstResponse).isEqualTo(secondResponse);
        assertThat(cachedValue).isEqualTo(firstResponse);
    }

    @Test
    void 지역으로_대학을_필터링한다() {
        // when
        UniversityInfoForApplyPreviewResponses response = universityService.searchUniversity(
                영미권.getCode(), List.of(), null, null);

        // then
        assertThat(response.universityInfoForApplyPreviewResponses())
                .containsExactlyInAnyOrder(
                        UniversityInfoForApplyPreviewResponse.from(괌대학_A_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(괌대학_B_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(네바다주립대학_라스베이거스_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(메모리얼대학_세인트존스_A_지원_정보)
                );
    }

    @Test
    void 키워드로_대학을_필터링한다() {
        // when
        UniversityInfoForApplyPreviewResponses response = universityService.searchUniversity(
                null, List.of("라", "일본"), null, null);

        // then
        assertThat(response.universityInfoForApplyPreviewResponses())
                .containsExactlyInAnyOrder(
                        UniversityInfoForApplyPreviewResponse.from(네바다주립대학_라스베이거스_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(그라츠대학_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(그라츠공과대학_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(메이지대학_지원_정보)
                );
    }

    @Test
    void 어학시험_조건으로_대학을_필터링한다() {
        // when
        UniversityInfoForApplyPreviewResponses response = universityService.searchUniversity(
                null, List.of(), LanguageTestType.TOEFL_IBT, "70");

        // then
        assertThat(response.universityInfoForApplyPreviewResponses())
                .containsExactlyInAnyOrder(
                        UniversityInfoForApplyPreviewResponse.from(괌대학_B_지원_정보),
                        UniversityInfoForApplyPreviewResponse.from(서던덴마크대학교_지원_정보)
                );
    }

    @Test
    void 모든_조건으로_대학을_필터링한다() {
        // when
        UniversityInfoForApplyPreviewResponses response = universityService.searchUniversity(
                "EUROPE", List.of(), LanguageTestType.TOEFL_IBT, "70");

        // then
        assertThat(response.universityInfoForApplyPreviewResponses())
                .containsExactly(UniversityInfoForApplyPreviewResponse.from(서던덴마크대학교_지원_정보));
    }
}
