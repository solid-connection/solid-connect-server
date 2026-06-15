package com.example.solidconnection.admin.university.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.admin.university.dto.UnivApplyInfoFieldResponse;
import com.example.solidconnection.admin.university.dto.UnivApplyInfoImportRequest;
import com.example.solidconnection.admin.university.dto.UnivApplyInfoImportResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.fixture.TermFixture;
import com.example.solidconnection.university.domain.HomeUniversity;
import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.domain.UnivApplyInfoColumn;
import com.example.solidconnection.university.fixture.HomeUniversityFixture;
import com.example.solidconnection.university.fixture.UniversityFixture;
import com.example.solidconnection.university.repository.LanguageRequirementRepository;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("UnivApplyInfo 임포트 서비스 테스트")
class AdminUnivApplyInfoServiceTest {

    @Autowired
    private AdminUnivApplyInfoService adminUnivApplyInfoService;

    @Autowired
    private UnivApplyInfoRepository univApplyInfoRepository;

    @Autowired
    private LanguageRequirementRepository languageRequirementRepository;

    @Autowired
    private TermFixture termFixture;

    @Autowired
    private HomeUniversityFixture homeUniversityFixture;

    @Autowired
    private UniversityFixture universityFixture;

    private Term term;
    private HomeUniversity homeUniversity;

    private static final String 괌_대학_한국명 = "괌 대학";
    private static final String 버지니아_대학_한국명 = "버지니아 공과 대학";
    private static final long invalidId = 999L;

    @BeforeEach
    void setUp() {
        term = termFixture.현재_학기("2025-2");
        homeUniversity = homeUniversityFixture.인하대학교();
        universityFixture.괌_대학();
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
                    () -> assertThat(response.structuredFields())
                            .hasSize(UnivApplyInfoColumn.values().length),
                    () -> assertThat(response.languageTestTypes())
                            .containsExactlyInAnyOrderElementsOf(
                                    Arrays.stream(LanguageTestType.values()).map(Enum::name).toList()
                            )
            );
        }

        @Test
        void studentCapacity_필드에_인원_alias가_포함된다() {
            // when
            UnivApplyInfoFieldResponse response = adminUnivApplyInfoService.getFields();

            // then
            UnivApplyInfoFieldResponse.FieldInfo field = response.structuredFields().stream()
                    .filter(f -> "studentCapacity".equals(f.field()))
                    .findFirst()
                    .orElseThrow();
            assertThat(field.aliases()).contains("인원", "모집인원", "정원");
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
                    () -> assertThat(response.failedRows()).isEmpty(),
                    () -> assertThat(univApplyInfoRepository.findAll()).hasSize(2)
            );
        }

        @Test
        void enum_변환_실패시_해당_컬럼은_extraInfo에_저장된다() {
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

            // when
            UnivApplyInfoImportResponse response = adminUnivApplyInfoService.importUnivApplyInfos(request);

            // then
            UnivApplyInfo saved = univApplyInfoRepository.findAll().get(0);
            assertAll(
                    () -> assertThat(response.successCount()).isEqualTo(1),
                    () -> assertThat(response.failedRows()).isEmpty(),
                    () -> assertThat(saved.getSemesterAvailableForDispatch()).isNull(),
                    () -> assertThat(saved.getExtraInfo()).containsEntry("파견가능학기", "알수없음")
            );
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
        void 존재하지_않는_대학명_행만_실패하고_나머지는_저장된다() {
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

            // when
            UnivApplyInfoImportResponse response = adminUnivApplyInfoService.importUnivApplyInfos(request);

            // then
            assertAll(
                    () -> assertThat(response.successCount()).isEqualTo(2),
                    () -> assertThat(response.failedRows()).hasSize(1),
                    () -> assertThat(response.failedRows().get(0).rowNumber()).isEqualTo(2),
                    () -> assertThat(univApplyInfoRepository.findAll()).hasSize(2)
            );
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
    }
}
