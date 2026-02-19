package com.example.solidconnection.university.service;

import static com.example.solidconnection.common.exception.ErrorCode.UNIV_APPLY_INFO_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.fixture.TermFixture;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.dto.UnivApplyInfoDetailResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponse;
import com.example.solidconnection.university.dto.UnivApplyInfoPreviewResponses;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixture;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
    private TermFixture termFixture;

    private Term term;

    @BeforeEach
    void setUp() {
        term = termFixture.현재_학기("2025-2");
    }

    @Nested
    class 대학_지원_정보_상세_조회 {

        @Test
        void 대학_지원_정보를_상세_조회한다() {
            // given
            UnivApplyInfo 괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보(term.getId());

            // when
            UnivApplyInfoDetailResponse response = univApplyInfoQueryService.getUnivApplyInfoDetail(괌대학_A_지원_정보.getId());

            // then
            assertThat(response.id()).isEqualTo(괌대학_A_지원_정보.getId());
        }

        @Test
        void 대학_지원_정보_상세_조회시_캐시가_적용된다() {
            // given
            UnivApplyInfo 괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보(term.getId());

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
    class 대학_지원_정보_텍스트_검색 {

        @Test
        void 텍스트가_없으면_전체_대학을_id_순으로_정렬하여_반환한다() {
            // given
            UnivApplyInfo 괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보(term.getId());
            UnivApplyInfo 메이지대학_지원_정보 = univApplyInfoFixture.메이지대학_지원_정보(term.getId());

            // when
            UnivApplyInfoPreviewResponses response = univApplyInfoQueryService.searchUnivApplyInfoByText(null);

            // then
            assertThat(response.univApplyInfoPreviews())
                    .containsExactly(
                            UnivApplyInfoPreviewResponse.of(괌대학_A_지원_정보, term.getName()),
                            UnivApplyInfoPreviewResponse.of(메이지대학_지원_정보, term.getName())
                    );
        }

        @Nested
        class 각각의_검색_대상에_대해_검색한다 {

            @Test
            void 국문_대학_지원_정보명() {
                // given
                String text = "메";
                UnivApplyInfo 메이지대학_지원_정보 = univApplyInfoFixture.메이지대학_지원_정보(term.getId());
                UnivApplyInfo 메모리얼대학_세인트존스_A_지원_정보 = univApplyInfoFixture.메모리얼대학_세인트존스_A_지원_정보(term.getId());
                univApplyInfoFixture.괌대학_A_지원_정보(term.getId());

                // when
                UnivApplyInfoPreviewResponses response = univApplyInfoQueryService.searchUnivApplyInfoByText(text);

                // then
                assertThat(response.univApplyInfoPreviews())
                        .containsExactly(
                                UnivApplyInfoPreviewResponse.of(메이지대학_지원_정보, term.getName()),
                                UnivApplyInfoPreviewResponse.of(메모리얼대학_세인트존스_A_지원_정보, term.getName())
                        );
            }

            @Test
            void 국문_국가명() {
                // given
                String text = "미국";
                UnivApplyInfo 괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보(term.getId());
                UnivApplyInfo 버지니아공과대학_지원_정보 = univApplyInfoFixture.버지니아공과대학_지원_정보(term.getId());
                univApplyInfoFixture.메이지대학_지원_정보(term.getId());

                // when
                UnivApplyInfoPreviewResponses response = univApplyInfoQueryService.searchUnivApplyInfoByText(text);

                // then
                assertThat(response.univApplyInfoPreviews())
                        .containsExactly(
                                UnivApplyInfoPreviewResponse.of(괌대학_A_지원_정보, term.getName()),
                                UnivApplyInfoPreviewResponse.of(버지니아공과대학_지원_정보, term.getName())
                        );
            }

            @Test
            void 국문_권역명() {
                // given
                String text = "유럽";
                UnivApplyInfo 린츠_카톨릭대학_지원_정보 = univApplyInfoFixture.린츠_카톨릭대학_지원_정보(term.getId());
                UnivApplyInfo 서던덴마크대학교_지원_정보 = univApplyInfoFixture.서던덴마크대학교_지원_정보(term.getId());
                univApplyInfoFixture.메이지대학_지원_정보(term.getId());

                // when
                UnivApplyInfoPreviewResponses response = univApplyInfoQueryService.searchUnivApplyInfoByText(text);

                // then
                assertThat(response.univApplyInfoPreviews())
                        .containsExactly(
                                UnivApplyInfoPreviewResponse.of(린츠_카톨릭대학_지원_정보, term.getName()),
                                UnivApplyInfoPreviewResponse.of(서던덴마크대학교_지원_정보, term.getName())
                        );
            }
        }

        @Test
        void 대학_국가_권역_일치_순서로_정렬하여_응답한다() {
            // given
            String text = "아";
            UnivApplyInfo 권역_아 = univApplyInfoFixture.메이지대학_지원_정보(term.getId());
            UnivApplyInfo 국가_아 = univApplyInfoFixture.그라츠대학_지원_정보(term.getId());
            UnivApplyInfo 대학지원정보_아 = univApplyInfoFixture.아칸소주립대학_지원_정보(term.getId());

            // when
            UnivApplyInfoPreviewResponses response = univApplyInfoQueryService.searchUnivApplyInfoByText(text);

            // then
            assertThat(response.univApplyInfoPreviews())
                    .containsExactly(
                            UnivApplyInfoPreviewResponse.of(대학지원정보_아, term.getName()),
                            UnivApplyInfoPreviewResponse.of(국가_아, term.getName()),
                            UnivApplyInfoPreviewResponse.of(권역_아, term.getName())
                    );
        }

        @Test
        void 캐시가_적용된다() {
            // given
            String text = "Guam";
            UnivApplyInfo 괌대학_A_지원_정보 = univApplyInfoFixture.괌대학_A_지원_정보(term.getId());

            // when
            UnivApplyInfoPreviewResponses firstResponse = univApplyInfoQueryService.searchUnivApplyInfoByText(text);
            UnivApplyInfoPreviewResponses secondResponse = univApplyInfoQueryService.searchUnivApplyInfoByText(text);

            // then
            assertThatCode(() -> {
                List<Long> firstResponseIds = extractIds(firstResponse);
                List<Long> secondResponseIds = extractIds(secondResponse);
                assertThat(firstResponseIds).isEqualTo(secondResponseIds);
            }).doesNotThrowAnyException();
            then(univApplyInfoRepository).should(times(1)).findAllByText(text, term.getId());
        }

        private List<Long> extractIds(UnivApplyInfoPreviewResponses responses) {
            return responses.univApplyInfoPreviews()
                    .stream()
                    .map(UnivApplyInfoPreviewResponse::id)
                    .toList();
        }
    }
}
