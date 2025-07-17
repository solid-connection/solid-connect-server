package com.example.solidconnection.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.UNIV_APPLY_INFO_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.dto.UnivApplyInfoDetailResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponses;
import com.example.solidconnection.university.fixture.LanguageRequirementFixture;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixture;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import com.example.solidconnection.university.repository.custom.UnivApplyInfoFilterRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

@TestContainerSpringBootTest
@DisplayName("대학 지원 정보 조회 서비스 테스트")
class UnivApplyInfoQueryServiceTest {

    @Autowired
    private UnivApplyInfoQueryService univApplyInfoQueryService;

    @SpyBean
    private UnivApplyInfoFilterRepository univApplyInfoFilterRepository;

    @SpyBean
    private UnivApplyInfoRepository univApplyInfoRepository;

    @Autowired
    private UnivApplyInfoFixture univApplyInfoFixture;

    @Autowired
    private LanguageRequirementFixture languageRequirementFixture;

    @Test
    void 대학_지원_정보를_상세_조회한다() {
        // given
        UnivApplyInfo 괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보();

        // when
        UnivApplyInfoDetailResponse response = univApplyInfoQueryService.getUnivApplyInfoDetail(괌대학_A_지원_정보.getId());

        // then
        assertThat(response.id()).isEqualTo(괌대학_A_지원_정보.getId());
    }

    @Test
    void 대학_지원_정보_상세_조회시_캐시가_적용된다() {
        // given
        UnivApplyInfo 괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보();

        // when
        UnivApplyInfoDetailResponse firstResponse = univApplyInfoQueryService.getUnivApplyInfoDetail(괌대학_A_지원_정보.getId());
        UnivApplyInfoDetailResponse secondResponse = univApplyInfoQueryService.getUnivApplyInfoDetail(괌대학_A_지원_정보.getId());

        // then
        assertThat(firstResponse).isEqualTo(secondResponse);
        then(univApplyInfoRepository).should(times(1)).getUnivApplyInfoById(괌대학_A_지원_정보.getId());
    }

    @Test
    void 존재하지_않는_대학_지원_정보를_조회하면_예외가_발생한다() {
        // given
        Long invalidUnivApplyInfoId = 9999L;

        // when & then
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> univApplyInfoQueryService.getUnivApplyInfoDetail(invalidUnivApplyInfoId))
                .havingRootCause()
                .isInstanceOf(CustomException.class)
                .withMessage(UNIV_APPLY_INFO_NOT_FOUND.getMessage());
    }

    @Test
    void 전체_대학_지원_정보를_조회한다() {
        // given
        UnivApplyInfo 괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보();
        UnivApplyInfo 괌대학_B_지원_정보 = univApplyInfoFixture.괌대학_B_지원_정보();
        UnivApplyInfo 네바다주립대학_라스베이거스_지원_정보 = univApplyInfoFixture.네바다주립대학_라스베이거스_지원_정보();
        UnivApplyInfo 서던덴마크대학교_지원_정보 = univApplyInfoFixture.서던덴마크대학교_지원_정보();
        UnivApplyInfo 그라츠대학_지원_정보 = univApplyInfoFixture.그라츠대학_지원_정보();
        UnivApplyInfo 메이지대학_지원_정보 = univApplyInfoFixture.메이지대학_지원_정보();

        // when
        UnivApplyInfoPreviewResponses response = univApplyInfoQueryService.searchUnivApplyInfo(
                null, List.of(), null, null);

        // then
        assertThat(response.univApplyInfoPreviews())
                .containsExactlyInAnyOrder(
                        UnivApplyInfoPreviewResponse.from(괌대학_A_지원_정보),
                        UnivApplyInfoPreviewResponse.from(괌대학_B_지원_정보),
                        UnivApplyInfoPreviewResponse.from(네바다주립대학_라스베이거스_지원_정보),
                        UnivApplyInfoPreviewResponse.from(서던덴마크대학교_지원_정보),
                        UnivApplyInfoPreviewResponse.from(그라츠대학_지원_정보),
                        UnivApplyInfoPreviewResponse.from(메이지대학_지원_정보)
                );
    }

    @Test
    void 대학_지원_정보_조회시_캐시가_적용된다() {
        // given
        univApplyInfoFixture.괌대학_A_지원_정보();
        String regionCode = "AMERICAS";
        List<String> keywords = List.of("괌");
        LanguageTestType testType = LanguageTestType.TOEFL_IBT;
        String testScore = "70";
        String term = "2024-1";

        // when
        UnivApplyInfoPreviewResponses firstResponse =
                univApplyInfoQueryService.searchUnivApplyInfo(regionCode, keywords, testType, testScore);
        UnivApplyInfoPreviewResponses secondResponse =
                univApplyInfoQueryService.searchUnivApplyInfo(regionCode, keywords, testType, testScore);

        // then
        assertThat(firstResponse).isEqualTo(secondResponse);
        then(univApplyInfoFilterRepository).should(times(1))
                .findAllByRegionCodeAndKeywordsAndLanguageTestTypeAndTestScoreAndTerm(
                        regionCode, keywords, testType, testScore, term);
    }

    @Test
    void 지역으로_대학_지원_정보를_필터링한다() {
        // given
        UnivApplyInfo 괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보();
        univApplyInfoFixture.코펜하겐IT대학_지원_정보();
        univApplyInfoFixture.그라츠공과대학_지원_정보();
        univApplyInfoFixture.메이지대학_지원_정보();

        // when
        UnivApplyInfoPreviewResponses response = univApplyInfoQueryService.searchUnivApplyInfo(
                "AMERICAS", List.of(), null, null);

        // then
        assertThat(response.univApplyInfoPreviews())
                .containsExactlyInAnyOrder(UnivApplyInfoPreviewResponse.from(괌대학_A_지원_정보));
    }

    @Test
    void 키워드로_대학_지원_정보를_필터링한다() {
        // given
        univApplyInfoFixture.괌대학_A_지원_정보();
        UnivApplyInfo 그라츠대학_지원_정보 = univApplyInfoFixture.그라츠대학_지원_정보();
        UnivApplyInfo 메이지대학_지원_정보 = univApplyInfoFixture.메이지대학_지원_정보();

        // when
        UnivApplyInfoPreviewResponses response = univApplyInfoQueryService.searchUnivApplyInfo(
                null, List.of("라", "일본"), null, null);

        // then
        assertThat(response.univApplyInfoPreviews())
                .containsExactlyInAnyOrder(
                        UnivApplyInfoPreviewResponse.from(그라츠대학_지원_정보),
                        UnivApplyInfoPreviewResponse.from(메이지대학_지원_정보)
                );
    }

    @Test
    void 어학시험_조건으로_대학_지원_정보를_필터링한다() {
        // given
        UnivApplyInfo 괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보();
        languageRequirementFixture.토플_80(괌대학_A_지원_정보);
        languageRequirementFixture.토익_800(괌대학_A_지원_정보);
        UnivApplyInfo 괌대학_B_지원_정보 = univApplyInfoFixture.괌대학_B_지원_정보();
        languageRequirementFixture.토플_70(괌대학_B_지원_정보);
        languageRequirementFixture.토익_900(괌대학_B_지원_정보);

        // when
        UnivApplyInfoPreviewResponses response = univApplyInfoQueryService.searchUnivApplyInfo(
                null, List.of(), LanguageTestType.TOEFL_IBT, "70");

        // then
        assertThat(response.univApplyInfoPreviews())
                .containsExactlyInAnyOrder(UnivApplyInfoPreviewResponse.from(괌대학_B_지원_정보));
    }

    @Test
    void 모든_조건으로_대학_지원_정보를_필터링한다() {
        // given
        UnivApplyInfo 괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보();
        languageRequirementFixture.토플_80(괌대학_A_지원_정보);
        languageRequirementFixture.토익_800(괌대학_A_지원_정보);
        UnivApplyInfo 서던덴마크대학교_지원_정보 = univApplyInfoFixture.서던덴마크대학교_지원_정보();
        languageRequirementFixture.토플_70(서던덴마크대학교_지원_정보);

        // when
        UnivApplyInfoPreviewResponses response = univApplyInfoQueryService.searchUnivApplyInfo(
                "EUROPE", List.of(), LanguageTestType.TOEFL_IBT, "70");

        // then
        assertThat(response.univApplyInfoPreviews())
                .containsExactly(UnivApplyInfoPreviewResponse.from(서던덴마크대학교_지원_정보));
    }
}
