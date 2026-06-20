package com.example.solidconnection.admin.university.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.example.solidconnection.admin.university.dto.AdminUnivApplyInfoCreateRequest;
import com.example.solidconnection.admin.university.dto.AdminUnivApplyInfoLanguageRequirementRequest;
import com.example.solidconnection.admin.university.dto.AdminUnivApplyInfoResponse;
import com.example.solidconnection.admin.university.dto.AdminUnivApplyInfoUpdateRequest;
import com.example.solidconnection.admin.university.dto.UnivApplyInfoFieldResponse;
import com.example.solidconnection.admin.university.dto.UnivApplyInfoImportRequest;
import com.example.solidconnection.admin.university.dto.UnivApplyInfoImportResponse;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.application.fixture.ApplicationFixture;
import com.example.solidconnection.cache.manager.CustomCacheManager;
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
import com.example.solidconnection.university.fixture.UnivApplyInfoFixtureBuilder;
import com.example.solidconnection.university.fixture.UniversityFixture;
import com.example.solidconnection.university.repository.LanguageRequirementRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@TestContainerSpringBootTest
@DisplayName("UnivApplyInfo 서비스 테스트")
class AdminUnivApplyInfoServiceTest {

    @Autowired
    private AdminUnivApplyInfoService adminUnivApplyInfoService;

    @Autowired
    private UnivApplyInfoRepository univApplyInfoRepository;

    @Autowired
    private LanguageRequirementRepository languageRequirementRepository;

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
    private SiteUserFixture siteUserFixture;

    @Autowired
    private ApplicationFixture applicationFixture;

    @MockitoSpyBean
    private CustomCacheManager cacheManager;

    private Term term;
    private HomeUniversity homeUniversity;
    private HostUniversity hostUniversity;

    private static final String 괌_대학_한국명 = "괌 대학";
    private static final String 버지니아_대학_한국명 = "버지니아 공과 대학";
    private static final long invalidId = 999L;

    @BeforeEach
    void setUp() {
        term = termFixture.현재_학기("2025-2");
        homeUniversity = homeUniversityFixture.인하대학교();
        hostUniversity = universityFixture.괌_대학();
        universityFixture.버지니아_공과_대학();
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
    class UnivApplyInfo를_임포트한다 {

        @Test
        void 모든_행이_정상_저장된다() {
            // given
            String markdown = String.format("""
                    | 대학명 | 인원 |
                    |--------|------|
                    | %s | 2 |
                    | %s | 3 |
                    """, 괌_대학_한국명, 버지니아_대학_한국명);
            UnivApplyInfoImportRequest request = new UnivApplyInfoImportRequest(
                    term.getId(), homeUniversity.getId(), markdown,
                    Map.of("대학명", "universityKoreanName", "인원", "studentCapacity")
            );

            // when
            UnivApplyInfoImportResponse response = adminUnivApplyInfoService.importUnivApplyInfos(request);

            // then
            assertAll(
                    () -> assertThat(response.successCount()).isEqualTo(2),
                    () -> assertThat(univApplyInfoRepository.findAll()).hasSize(2)
            );
        }

        @Test
        void 임포트_성공_시_검색과_추천_캐시가_무효화된다() {
            // given
            String markdown = String.format("""
                    | 대학명 | 인원 |
                    |--------|------|
                    | %s | 2 |
                    """, 괌_대학_한국명);
            UnivApplyInfoImportRequest request = new UnivApplyInfoImportRequest(
                    term.getId(), homeUniversity.getId(), markdown,
                    Map.of("대학명", "universityKoreanName", "인원", "studentCapacity")
            );

            // when
            adminUnivApplyInfoService.importUnivApplyInfos(request);

            // then
            then(cacheManager).should(times(1)).evictUsingPrefix("univApplyInfoTextSearch");
            then(cacheManager).should(times(1)).evictUsingPrefix("university:recommend:general");
        }

        @Test
        void enum_변환_실패시_전체가_실패한다() {
            // given
            String markdown = String.format("""
                    | 대학명 | 파견가능학기 |
                    |--------|------------|
                    | %s | 알수없음 |
                    """, 괌_대학_한국명);
            UnivApplyInfoImportRequest request = new UnivApplyInfoImportRequest(
                    term.getId(), homeUniversity.getId(), markdown,
                    Map.of("대학명", "universityKoreanName", "파견가능학기", "semesterAvailableForDispatch")
            );

            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.importUnivApplyInfos(request))
                    .isInstanceOf(CustomException.class);
            assertThat(univApplyInfoRepository.findAll()).isEmpty();
        }

        @Test
        void 어학시험_컬럼은_LanguageRequirement로_저장된다() {
            // given
            String markdown = String.format("""
                    | 대학명 | TOEIC |
                    |--------|-------|
                    | %s | 800 |
                    """, 괌_대학_한국명);
            UnivApplyInfoImportRequest request = new UnivApplyInfoImportRequest(
                    term.getId(), homeUniversity.getId(), markdown,
                    Map.of("대학명", "universityKoreanName", "TOEIC", "TOEIC")
            );

            // when
            adminUnivApplyInfoService.importUnivApplyInfos(request);

            // then
            assertThat(languageRequirementRepository.findAll())
                    .anyMatch(lr -> lr.getLanguageTestType() == LanguageTestType.TOEIC
                            && "800".equals(lr.getMinScore()));
        }

        @Test
        void extraInfo_매핑_컬럼은_extraInfo에_저장된다() {
            // given
            String markdown = String.format("""
                    | 대학명 | 특이사항 |
                    |--------|----------|
                    | %s | 주의 필요 |
                    """, 괌_대학_한국명);
            UnivApplyInfoImportRequest request = new UnivApplyInfoImportRequest(
                    term.getId(), homeUniversity.getId(), markdown,
                    Map.of("대학명", "universityKoreanName", "특이사항", "extraInfo")
            );

            // when
            adminUnivApplyInfoService.importUnivApplyInfos(request);

            // then
            UnivApplyInfo saved = univApplyInfoRepository.findAll().get(0);
            assertThat(saved.getExtraInfo()).containsEntry("특이사항", "주의 필요");
        }

        @Test
        void columnMappings에_없는_컬럼은_extraInfo에_저장된다() {
            // given
            String markdown = String.format("""
                    | 대학명 | 미매핑컬럼 |
                    |--------|------------|
                    | %s | 어떤값 |
                    """, 괌_대학_한국명);
            UnivApplyInfoImportRequest request = new UnivApplyInfoImportRequest(
                    term.getId(), homeUniversity.getId(), markdown,
                    Map.of("대학명", "universityKoreanName")
            );

            // when
            adminUnivApplyInfoService.importUnivApplyInfos(request);

            // then
            UnivApplyInfo saved = univApplyInfoRepository.findAll().get(0);
            assertThat(saved.getExtraInfo()).containsEntry("미매핑컬럼", "어떤값");
        }

        @Test
        void 존재하지_않는_대학명이_있으면_전체가_실패한다() {
            // given
            String markdown = String.format("""
                    | 대학명 | 인원 |
                    |--------|------|
                    | %s | 2 |
                    | 존재하지않는대학교 | 1 |
                    | %s | 3 |
                    """, 괌_대학_한국명, 버지니아_대학_한국명);
            UnivApplyInfoImportRequest request = new UnivApplyInfoImportRequest(
                    term.getId(), homeUniversity.getId(), markdown,
                    Map.of("대학명", "universityKoreanName", "인원", "studentCapacity")
            );

            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.importUnivApplyInfos(request))
                    .isInstanceOf(CustomException.class);
            assertThat(univApplyInfoRepository.findAll()).isEmpty();
        }

        @Test
        void 존재하지_않는_국가코드면_전체가_실패한다() {
            // given
            String markdown = """
                    | 대학명 | 국가코드 |
                    |--------|----------|
                    | 새 대학교 | ZZ |
                    """;
            UnivApplyInfoImportRequest request = new UnivApplyInfoImportRequest(
                    term.getId(), homeUniversity.getId(), markdown,
                    Map.of("대학명", "universityKoreanName", "국가코드", "universityCountryCode")
            );

            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.importUnivApplyInfos(request))
                    .isInstanceOf(CustomException.class);
            assertThat(univApplyInfoRepository.findAll()).isEmpty();
        }

        @Test
        void 대학명이_비어있으면_전체가_실패한다() {
            // given
            String markdown = """
                    | 대학명 | 국가코드 |
                    |--------|----------|
                    |  | Belgium |
                    """;
            UnivApplyInfoImportRequest request = new UnivApplyInfoImportRequest(
                    term.getId(), homeUniversity.getId(), markdown,
                    Map.of("대학명", "universityKoreanName", "국가코드", "universityCountryCode")
            );

            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.importUnivApplyInfos(request))
                    .isInstanceOf(CustomException.class);
            assertThat(univApplyInfoRepository.findAll()).isEmpty();
        }

        @Test
        void 구분자_없는_마크다운이면_예외_응답을_반환한다() {
            // given
            String invalidMarkdown = "| 대학명 |\n| MIT |";
            UnivApplyInfoImportRequest request = new UnivApplyInfoImportRequest(
                    term.getId(), homeUniversity.getId(), invalidMarkdown, Map.of()
            );

            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.importUnivApplyInfos(request))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void 존재하지_않는_termId이면_예외_응답을_반환한다() {
            // given
            String markdown = String.format("""
                    | 대학명 |
                    |--------|
                    | %s |
                    """, 괌_대학_한국명);
            UnivApplyInfoImportRequest request = new UnivApplyInfoImportRequest(
                    invalidId, homeUniversity.getId(), markdown,
                    Map.of("대학명", "universityKoreanName")
            );

            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.importUnivApplyInfos(request))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void 존재하지_않는_homeUniversityId이면_예외_응답을_반환한다() {
            // given
            String markdown = String.format("""
                    | 대학명 |
                    |--------|
                    | %s |
                    """, 괌_대학_한국명);
            UnivApplyInfoImportRequest request = new UnivApplyInfoImportRequest(
                    term.getId(), invalidId, markdown,
                    Map.of("대학명", "universityKoreanName")
            );

            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.importUnivApplyInfos(request))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void 선발인원에_정수가_아닌_값이_들어오면_전체가_실패한다() {
            // given
            String markdown = String.format("""
                    | 대학명 | 인원 |
                    |--------|------|
                    | %s | School of Business |
                    """, 괌_대학_한국명);
            UnivApplyInfoImportRequest request = new UnivApplyInfoImportRequest(
                    term.getId(), homeUniversity.getId(), markdown,
                    Map.of("대학명", "universityKoreanName", "인원", "studentCapacity")
            );

            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.importUnivApplyInfos(request))
                    .isInstanceOf(CustomException.class);
            assertThat(univApplyInfoRepository.findAll()).isEmpty();
        }

        @Test
        void 길이_제한을_초과하는_값이_들어오면_전체가_실패한다() {
            // given
            String tooLongValue = "a".repeat(101);
            String markdown = String.format("""
                    | 대학명 | 학기요건 |
                    |--------|----------|
                    | %s | %s |
                    """, 괌_대학_한국명, tooLongValue);
            UnivApplyInfoImportRequest request = new UnivApplyInfoImportRequest(
                    term.getId(), homeUniversity.getId(), markdown,
                    Map.of("대학명", "universityKoreanName", "학기요건", "semesterRequirement")
            );

            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.importUnivApplyInfos(request))
                    .isInstanceOf(CustomException.class);
            assertThat(univApplyInfoRepository.findAll()).isEmpty();
        }

        @Test
        void 파싱_오류가_있는_행이_있으면_전체가_실패한다() {
            // given
            String tooLong = "a".repeat(101);
            String markdown = String.format("""
                    | 대학명 | 인원 | 학기요건 |
                    |--------|------|----------|
                    | %s | 정수아님 | %s |
                    """, 괌_대학_한국명, tooLong);
            UnivApplyInfoImportRequest request = new UnivApplyInfoImportRequest(
                    term.getId(), homeUniversity.getId(), markdown,
                    Map.of(
                        "대학명", "universityKoreanName",
                        "인원", "studentCapacity",
                        "학기요건", "semesterRequirement"
                    )
            );

            // when & then
            assertThatCode(() -> adminUnivApplyInfoService.importUnivApplyInfos(request))
                    .isInstanceOf(CustomException.class);
            assertThat(univApplyInfoRepository.findAll()).isEmpty();
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
                    () -> assertThat(response.id()).isNotNull(),
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
