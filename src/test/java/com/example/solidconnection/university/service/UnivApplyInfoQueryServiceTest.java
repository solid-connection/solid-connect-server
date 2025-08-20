package com.example.solidconnection.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.UNIV_APPLY_INFO_NOT_FOUND;
import static com.example.solidconnection.university.domain.LanguageTestType.TOEIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.dto.UnivApplyInfoDetailResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoFilterSearchRequest;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponses;
import com.example.solidconnection.university.fixture.LanguageRequirementFixture;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixture;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

@TestContainerSpringBootTest
@DisplayName("대학 지원 정보 조회 서비스 테스트")
class UnivApplyInfoQueryServiceTest {

    @Autowired
    private UnivApplyInfoQueryService univApplyInfoQueryService;

    @SpyBean
    private UnivApplyInfoRepository univApplyInfoRepository;

    @Autowired
    private UnivApplyInfoFixture univApplyInfoFixture;

    @Autowired
    private LanguageRequirementFixture languageRequirementFixture;

    @Nested
    class 대학_지원_정보_상세_조회 {

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
    }

    @Nested
    class 대학_지원_정보_필터링_검색 {

        @Test
        void 어학_시험_종류로_필터링한다() {
            // given
            UnivApplyInfoFilterSearchRequest request = new UnivApplyInfoFilterSearchRequest(TOEIC, null, null);
            UnivApplyInfo 괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보();
            languageRequirementFixture.토익_800(괌대학_A_지원_정보);
            UnivApplyInfo 괌대학_B_지원_정보 = univApplyInfoFixture.괌대학_B_지원_정보();
            languageRequirementFixture.토플_70(괌대학_B_지원_정보);

            // when
            UnivApplyInfoPreviewResponses response = univApplyInfoQueryService.searchUnivApplyInfoByFilter(request);

            // then
            assertThat(response.univApplyInfoPreviews())
                    .containsExactly(UnivApplyInfoPreviewResponse.from(괌대학_A_지원_정보));
        }

        @Test
        void 어학_시험_점수가_기준치_이상인_곳을_필터링한다() {
            // given
            UnivApplyInfoFilterSearchRequest request = new UnivApplyInfoFilterSearchRequest(TOEIC, "800", null);
            UnivApplyInfo 괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보();
            languageRequirementFixture.토익_800(괌대학_A_지원_정보);
            UnivApplyInfo 괌대학_B_지원_정보 = univApplyInfoFixture.괌대학_B_지원_정보();
            languageRequirementFixture.토익_900(괌대학_B_지원_정보);

            // when
            UnivApplyInfoPreviewResponses response = univApplyInfoQueryService.searchUnivApplyInfoByFilter(request);

            // then
            assertThat(response.univApplyInfoPreviews())
                    .containsExactlyInAnyOrder(UnivApplyInfoPreviewResponse.from(괌대학_A_지원_정보));
        }

        @Test
        void 국가_코드로_필터링한다() {
            // given
            UnivApplyInfoFilterSearchRequest request1 = new UnivApplyInfoFilterSearchRequest(TOEIC, null, List.of("US"));
            UnivApplyInfoFilterSearchRequest request2 = new UnivApplyInfoFilterSearchRequest(TOEIC, null, List.of("US", "CA"));
            UnivApplyInfo 괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보();
            languageRequirementFixture.토익_800(괌대학_A_지원_정보);
            UnivApplyInfo 메모리얼대학_세인트존스_A_지원_정보 = univApplyInfoFixture.메모리얼대학_세인트존스_A_지원_정보();
            languageRequirementFixture.토익_800(메모리얼대학_세인트존스_A_지원_정보);

            // when
            UnivApplyInfoPreviewResponses response1 = univApplyInfoQueryService.searchUnivApplyInfoByFilter(request1);
            UnivApplyInfoPreviewResponses response2 = univApplyInfoQueryService.searchUnivApplyInfoByFilter(request2);

            // then
            assertAll(
                    () -> assertThat(response1.univApplyInfoPreviews())
                            .containsExactly(UnivApplyInfoPreviewResponse.from(괌대학_A_지원_정보)),
                    () -> assertThat(response2.univApplyInfoPreviews())
                            .containsExactlyInAnyOrder(
                                    UnivApplyInfoPreviewResponse.from(괌대학_A_지원_정보),
                                    UnivApplyInfoPreviewResponse.from(메모리얼대학_세인트존스_A_지원_정보)
                            )
            );
        }
    }
}
