package com.example.solidconnection.admin.university.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.admin.university.dto.AdminUnivApplyInfoCreateRequest;
import com.example.solidconnection.admin.university.dto.AdminUnivApplyInfoLanguageRequirementRequest;
import com.example.solidconnection.admin.university.dto.AdminUnivApplyInfoResponse;
import com.example.solidconnection.admin.university.dto.AdminUnivApplyInfoUpdateRequest;
import com.example.solidconnection.admin.university.dto.UnivApplyInfoFieldResponse;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.application.fixture.ApplicationFixture;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.fixture.TermFixture;
import com.example.solidconnection.university.domain.HomeUniversity;
import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.university.domain.LikedUnivApplyInfo;
import com.example.solidconnection.university.domain.SemesterAvailableForDispatch;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.domain.UnivApplyInfoColumn;
import com.example.solidconnection.university.fixture.HomeUniversityFixture;
import com.example.solidconnection.university.fixture.LanguageRequirementFixture;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixtureBuilder;
import com.example.solidconnection.university.fixture.UniversityFixture;
import com.example.solidconnection.university.repository.LikedUnivApplyInfoRepository;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("UnivApplyInfo 서비스 테스트")
class AdminUnivApplyInfoServiceTest {

    @Autowired
    private AdminUnivApplyInfoService adminUnivApplyInfoService;

    @Autowired
    private UnivApplyInfoRepository univApplyInfoRepository;

    @Autowired
    private LikedUnivApplyInfoRepository likedUnivApplyInfoRepository;

    @Autowired
    private TermFixture termFixture;

    @Autowired
    private HomeUniversityFixture homeUniversityFixture;

    @Autowired
    private UniversityFixture universityFixture;

    @Autowired
    private UnivApplyInfoFixtureBuilder univApplyInfoFixtureBuilder;

    @Autowired
    private LanguageRequirementFixture languageRequirementFixture;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private ApplicationFixture applicationFixture;

    private Term term;
    private HomeUniversity homeUniversity;
    private HostUniversity hostUniversity;
    private HostUniversity otherHostUniversity;

    private static final long invalidId = 999L;

    @BeforeEach
    void setUp() {
        term = termFixture.현재_학기("2025-2");
        homeUniversity = homeUniversityFixture.인하대학교();
        hostUniversity = universityFixture.괌_대학();
        otherHostUniversity = universityFixture.버지니아_공과_대학();
    }

    @Nested
    class 필드_목록을_조회한다 {

        @Test
        void 구조화_필드와_어학시험_타입을_반환한다() {
            // when
            UnivApplyInfoFieldResponse response = adminUnivApplyInfoService.getFields();

            // then
            assertAll(
                    () -> assertThat(response.fields())
                            .hasSize(UnivApplyInfoColumn.values().length),
                    () -> assertThat(response.languageTestTypes())
                            .containsExactlyInAnyOrderElementsOf(
                                    Arrays.stream(LanguageTestType.values()).map(Enum::name).toList()
                            )
            );
        }

    }

    @Nested
    class 지원_정보_생성 {

        @Test
        void 유효한_요청으로_지원_정보를_생성하면_성공한다() {
            // given
            AdminUnivApplyInfoCreateRequest request = new AdminUnivApplyInfoCreateRequest(
                    term.getId(), homeUniversity.getId(), hostUniversity.getId(),
                    5, SemesterAvailableForDispatch.ONE_SEMESTER,
                    "1학기 이상", "TOEIC 700 이상", "3.0 이상", "4.5",
                    "기숙사 제공", null, List.of()
            );

            // when
            AdminUnivApplyInfoResponse response = adminUnivApplyInfoService.createUnivApplyInfo(request);

            // then
            assertAll(
                    () -> assertThat(response.id()).isPositive(),
                    () -> assertThat(response.termId()).isEqualTo(term.getId()),
                    () -> assertThat(response.homeUniversityId()).isEqualTo(homeUniversity.getId()),
                    () -> assertThat(response.hostUniversityId()).isEqualTo(hostUniversity.getId()),
                    () -> assertThat(response.studentCapacity()).isEqualTo(5),
                    () -> assertThat(univApplyInfoRepository.findById(response.id())).isPresent()
            );
        }

        @Test
        void 언어_요건을_포함하여_생성하면_언어_요건도_저장된다() {
            // given
            var languageRequests = List.of(
                    new AdminUnivApplyInfoLanguageRequirementRequest(LanguageTestType.TOEIC, "700")
            );
            AdminUnivApplyInfoCreateRequest request = new AdminUnivApplyInfoCreateRequest(
                    term.getId(), homeUniversity.getId(), hostUniversity.getId(),
                    null, null, null, null, null, null, null, null, languageRequests
            );

            // when
            AdminUnivApplyInfoResponse response = adminUnivApplyInfoService.createUnivApplyInfo(request);

            // then
            assertThat(response.languageRequirements())
                    .hasSize(1)
                    .anyMatch(lr -> lr.languageTestType() == LanguageTestType.TOEIC
                            && "700".equals(lr.minScore()));
        }

        @Test
        void 존재하지_않는_termId로_생성하면_예외가_발생한다() {
            // given
            AdminUnivApplyInfoCreateRequest request = new AdminUnivApplyInfoCreateRequest(
                    invalidId, homeUniversity.getId(), hostUniversity.getId(),
                    null, null, null, null, null, null, null, null, List.of()
            );

            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.createUnivApplyInfo(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.TERM_NOT_FOUND.getMessage());
        }

        @Test
        void 존재하지_않는_homeUniversityId로_생성하면_예외가_발생한다() {
            // given
            AdminUnivApplyInfoCreateRequest request = new AdminUnivApplyInfoCreateRequest(
                    term.getId(), invalidId, hostUniversity.getId(),
                    null, null, null, null, null, null, null, null, List.of()
            );

            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.createUnivApplyInfo(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.HOME_UNIVERSITY_NOT_FOUND.getMessage());
        }

        @Test
        void 존재하지_않는_hostUniversityId로_생성하면_예외가_발생한다() {
            // given
            AdminUnivApplyInfoCreateRequest request = new AdminUnivApplyInfoCreateRequest(
                    term.getId(), homeUniversity.getId(), invalidId,
                    null, null, null, null, null, null, null, null, List.of()
            );

            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.createUnivApplyInfo(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.UNIVERSITY_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 지원_정보_단건_조회 {

        @Test
        void termId_homeUniversityId_hostUniversityId로_지원_정보를_조회한다() {
            // given
            UnivApplyInfo univApplyInfo = univApplyInfoFixtureBuilder.univApplyInfo()
                    .termId(term.getId()).koreanName("괌대학(A형)")
                    .university(hostUniversity).homeUniversity(homeUniversity).create();
            languageRequirementFixture.토플_80(univApplyInfo);
            univApplyInfoFixtureBuilder.univApplyInfo()
                    .termId(term.getId()).koreanName("버지니아공과대학")
                    .university(otherHostUniversity).homeUniversity(homeUniversity).create();

            // when
            List<AdminUnivApplyInfoResponse> responses = adminUnivApplyInfoService.findUnivApplyInfos(
                    term.getId(), homeUniversity.getId(), hostUniversity.getId());

            // then
            assertAll(
                    () -> assertThat(responses).hasSize(1),
                    () -> assertThat(responses.get(0).id()).isEqualTo(univApplyInfo.getId()),
                    () -> assertThat(responses.get(0).termId()).isEqualTo(term.getId()),
                    () -> assertThat(responses.get(0).homeUniversityId()).isEqualTo(homeUniversity.getId()),
                    () -> assertThat(responses.get(0).hostUniversityId()).isEqualTo(hostUniversity.getId()),
                    () -> assertThat(responses.get(0).languageRequirements())
                            .anyMatch(lr -> lr.languageTestType() == LanguageTestType.TOEFL_IBT
                                    && "80".equals(lr.minScore()))
            );
        }

        @Test
        void 일치하는_지원_정보가_없으면_빈_목록을_반환한다() {
            // given
            univApplyInfoFixtureBuilder.univApplyInfo()
                    .termId(term.getId()).koreanName("버지니아공과대학")
                    .university(otherHostUniversity).homeUniversity(homeUniversity).create();

            // when
            List<AdminUnivApplyInfoResponse> responses = adminUnivApplyInfoService.findUnivApplyInfos(
                    term.getId(), homeUniversity.getId(), hostUniversity.getId());

            // then
            assertThat(responses).isEmpty();
        }

        @Test
        void 존재하는_id로_조회하면_상세_정보를_반환한다() {
            // given
            UnivApplyInfo univApplyInfo = univApplyInfoFixtureBuilder.univApplyInfo()
                    .termId(term.getId()).koreanName("괌대학(A형)")
                    .university(hostUniversity).homeUniversity(homeUniversity).create();

            // when
            AdminUnivApplyInfoResponse response = adminUnivApplyInfoService.getUnivApplyInfo(univApplyInfo.getId());

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(univApplyInfo.getId()),
                    () -> assertThat(response.termId()).isEqualTo(term.getId()),
                    () -> assertThat(response.homeUniversityId()).isEqualTo(homeUniversity.getId()),
                    () -> assertThat(response.hostUniversityId()).isEqualTo(hostUniversity.getId()),
                    () -> assertThat(response.studentCapacity()).isEqualTo(univApplyInfo.getStudentCapacity()),
                    () -> assertThat(response.semesterRequirement()).isEqualTo(univApplyInfo.getSemesterRequirement()),
                    () -> assertThat(response.detailsForLanguage()).isEqualTo(univApplyInfo.getDetailsForLanguage()),
                    () -> assertThat(response.gpaRequirement()).isEqualTo(univApplyInfo.getGpaRequirement()),
                    () -> assertThat(response.gpaRequirementCriteria()).isEqualTo(univApplyInfo.getGpaRequirementCriteria()),
                    () -> assertThat(response.detailsForAccommodation()).isEqualTo(univApplyInfo.getDetailsForAccommodation())
            );
        }

        @Test
        void 존재하지_않는_id로_조회하면_예외가_발생한다() {
            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.getUnivApplyInfo(invalidId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.UNIV_APPLY_INFO_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 지원_정보_수정 {

        @Test
        void 유효한_요청으로_지원_정보를_수정하면_성공한다() {
            // given
            UnivApplyInfo univApplyInfo = univApplyInfoFixtureBuilder.univApplyInfo()
                    .termId(term.getId()).koreanName("괌대학(A형)")
                    .university(hostUniversity).homeUniversity(homeUniversity).create();
            AdminUnivApplyInfoUpdateRequest request = new AdminUnivApplyInfoUpdateRequest(
                    10, SemesterAvailableForDispatch.TWO_SEMESTER,
                    "2학기 이상", "TOEFL 80 이상", "3.5 이상", "4.5",
                    "기숙사 없음", Map.of("비고", "테스트"), List.of()
            );

            // when
            AdminUnivApplyInfoResponse response = adminUnivApplyInfoService.updateUnivApplyInfo(univApplyInfo.getId(), request);

            // then
            assertAll(
                    () -> assertThat(response.studentCapacity()).isEqualTo(10),
                    () -> assertThat(response.semesterAvailableForDispatch()).isEqualTo(SemesterAvailableForDispatch.TWO_SEMESTER),
                    () -> assertThat(response.extraInfo()).containsEntry("비고", "테스트")
            );
        }

        @Test
        void 수정_시_언어_요건이_기존_것과_교체된다() {
            // given
            UnivApplyInfo univApplyInfo = univApplyInfoFixtureBuilder.univApplyInfo()
                    .termId(term.getId()).koreanName("괌대학(A형)")
                    .university(hostUniversity).homeUniversity(homeUniversity).create();
            var newLanguageRequirements = List.of(
                    new AdminUnivApplyInfoLanguageRequirementRequest(LanguageTestType.TOEFL_IBT, "80")
            );
            AdminUnivApplyInfoUpdateRequest request = new AdminUnivApplyInfoUpdateRequest(
                    null, null, null, null, null, null, null, null, newLanguageRequirements
            );

            // when
            AdminUnivApplyInfoResponse response = adminUnivApplyInfoService.updateUnivApplyInfo(univApplyInfo.getId(), request);

            // then
            assertThat(response.languageRequirements())
                    .hasSize(1)
                    .anyMatch(lr -> lr.languageTestType() == LanguageTestType.TOEFL_IBT
                            && "80".equals(lr.minScore()));
        }

        @Test
        void 존재하지_않는_id로_수정하면_예외가_발생한다() {
            // given
            AdminUnivApplyInfoUpdateRequest request = new AdminUnivApplyInfoUpdateRequest(
                    null, null, null, null, null, null, null, null, List.of()
            );

            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.updateUnivApplyInfo(invalidId, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.UNIV_APPLY_INFO_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 지원_정보_삭제 {

        @Test
        void 참조가_없는_지원_정보를_삭제하면_성공한다() {
            // given
            UnivApplyInfo univApplyInfo = univApplyInfoFixtureBuilder.univApplyInfo()
                    .termId(term.getId()).koreanName("괌대학(A형)")
                    .university(hostUniversity).homeUniversity(homeUniversity).create();

            // when
            adminUnivApplyInfoService.deleteUnivApplyInfo(univApplyInfo.getId());

            // then
            assertThat(univApplyInfoRepository.findById(univApplyInfo.getId())).isEmpty();
        }

        @Test
        void 존재하지_않는_id로_삭제하면_예외가_발생한다() {
            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.deleteUnivApplyInfo(invalidId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.UNIV_APPLY_INFO_NOT_FOUND.getMessage());
        }

        @Test
        void LikedUnivApplyInfo가_참조하는_지원_정보를_삭제하면_예외가_발생한다() {
            // given
            UnivApplyInfo univApplyInfo = univApplyInfoFixtureBuilder.univApplyInfo()
                    .termId(term.getId()).koreanName("괌대학(A형)")
                    .university(hostUniversity).homeUniversity(homeUniversity).create();
            SiteUser siteUser = siteUserFixture.사용자();
            likedUnivApplyInfoRepository.save(
                    LikedUnivApplyInfo.builder()
                            .siteUserId(siteUser.getId())
                            .univApplyInfoId(univApplyInfo.getId())
                            .build()
            );

            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.deleteUnivApplyInfo(univApplyInfo.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.UNIV_APPLY_INFO_HAS_REFERENCES.getMessage());
        }

        @Test
        void ApplicationChoice가_참조하는_지원_정보를_삭제하면_예외가_발생한다() {
            // given
            UnivApplyInfo univApplyInfo = univApplyInfoFixtureBuilder.univApplyInfo()
                    .termId(term.getId()).koreanName("괌대학(A형)")
                    .university(hostUniversity).homeUniversity(homeUniversity).create();
            SiteUser siteUser = siteUserFixture.사용자();
            applicationFixture.지원서(
                    siteUser, "테스트닉네임", term.getId(),
                    new Gpa(4.0, 4.5, "url"),
                    new LanguageTest(LanguageTestType.TOEIC, "800", "url"),
                    List.of(univApplyInfo.getId())
            );

            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.deleteUnivApplyInfo(univApplyInfo.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.UNIV_APPLY_INFO_HAS_REFERENCES.getMessage());
        }
    }
}
