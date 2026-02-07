package com.example.solidconnection.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.example.solidconnection.admin.university.dto.AdminHostUniversityCreateRequest;
import com.example.solidconnection.admin.university.dto.AdminHostUniversityDetailResponse;
import com.example.solidconnection.admin.university.dto.AdminHostUniversityResponse;
import com.example.solidconnection.admin.university.dto.AdminHostUniversitySearchCondition;
import com.example.solidconnection.admin.university.dto.AdminHostUniversityUpdateRequest;
import com.example.solidconnection.admin.university.service.AdminHostUniversityService;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.location.country.domain.Country;
import com.example.solidconnection.location.country.fixture.CountryFixture;
import com.example.solidconnection.location.region.domain.Region;
import com.example.solidconnection.location.region.fixture.RegionFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixtureBuilder;
import com.example.solidconnection.university.fixture.UniversityFixture;
import com.example.solidconnection.university.repository.HostUniversityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@TestContainerSpringBootTest
@DisplayName("파견 대학 관리 서비스 테스트")
class AdminHostUniversityServiceTest {

    @Autowired
    private AdminHostUniversityService adminHostUniversityService;

    @Autowired
    private HostUniversityRepository hostUniversityRepository;

    @Autowired
    private UniversityFixture universityFixture;

    @Autowired
    private CountryFixture countryFixture;

    @Autowired
    private RegionFixture regionFixture;

    @Autowired
    private UnivApplyInfoFixtureBuilder univApplyInfoFixtureBuilder;

    @Nested
    class 목록_조회 {

        @Test
        void 대학이_없으면_빈_목록을_반환한다() {
            // given
            AdminHostUniversitySearchCondition condition = new AdminHostUniversitySearchCondition(null, null, null);

            // when
            Page<AdminHostUniversityResponse> response = adminHostUniversityService.getHostUniversities(
                    condition, PageRequest.of(0, 20));

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalElements()).isZero();
        }

        @Test
        void 키워드로_한글명을_검색한다() {
            // given
            universityFixture.괌_대학();
            HostUniversity target = universityFixture.메이지_대학();

            AdminHostUniversitySearchCondition condition = new AdminHostUniversitySearchCondition("메이지", null, null);

            // when
            Page<AdminHostUniversityResponse> response = adminHostUniversityService.getHostUniversities(
                    condition, PageRequest.of(0, 20));

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).koreanName()).isEqualTo(target.getKoreanName());
        }

        @Test
        void 키워드로_영문명을_검색한다() {
            // given
            universityFixture.괌_대학();
            HostUniversity target = universityFixture.메이지_대학();

            AdminHostUniversitySearchCondition condition = new AdminHostUniversitySearchCondition("Meiji", null, null);

            // when
            Page<AdminHostUniversityResponse> response = adminHostUniversityService.getHostUniversities(
                    condition, PageRequest.of(0, 20));

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).englishName()).isEqualTo(target.getEnglishName());
        }

        @Test
        void 국가_코드로_필터링한다() {
            // given
            universityFixture.괌_대학();
            universityFixture.네바다주립_대학_라스베이거스();
            universityFixture.메이지_대학();

            Country usa = countryFixture.미국();
            AdminHostUniversitySearchCondition condition = new AdminHostUniversitySearchCondition(null, usa.getCode(), null);

            // when
            Page<AdminHostUniversityResponse> response = adminHostUniversityService.getHostUniversities(
                    condition, PageRequest.of(0, 20));

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getContent())
                    .extracting(r -> r.countryCode())
                    .containsOnly(usa.getCode());
        }

        @Test
        void 지역_코드로_필터링한다() {
            // given
            universityFixture.괌_대학();
            universityFixture.서던덴마크_대학();
            universityFixture.그라츠_대학();

            Region europe = regionFixture.유럽();
            AdminHostUniversitySearchCondition condition = new AdminHostUniversitySearchCondition(null, null, europe.getCode());

            // when
            Page<AdminHostUniversityResponse> response = adminHostUniversityService.getHostUniversities(
                    condition, PageRequest.of(0, 20));

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getContent())
                    .extracting(r -> r.regionCode())
                    .containsOnly(europe.getCode());
        }

        @Test
        void 페이징이_정상_작동한다() {
            // given
            universityFixture.괌_대학();
            universityFixture.네바다주립_대학_라스베이거스();
            universityFixture.메이지_대학();

            AdminHostUniversitySearchCondition condition = new AdminHostUniversitySearchCondition(null, null, null);

            // when
            Page<AdminHostUniversityResponse> response = adminHostUniversityService.getHostUniversities(
                    condition, PageRequest.of(0, 2));

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getTotalElements()).isEqualTo(3);
            assertThat(response.getTotalPages()).isEqualTo(2);
            assertThat(response.hasNext()).isTrue();
        }
    }

    @Nested
    class 상세_조회 {

        @Test
        void 존재하는_대학을_조회하면_성공한다() {
            // given
            HostUniversity university = universityFixture.괌_대학();

            // when
            AdminHostUniversityDetailResponse response = adminHostUniversityService.getHostUniversity(university.getId());

            // then
            assertThat(response.id()).isEqualTo(university.getId());
            assertThat(response.koreanName()).isEqualTo(university.getKoreanName());
            assertThat(response.englishName()).isEqualTo(university.getEnglishName());
        }

        @Test
        void 존재하지_않는_대학을_조회하면_예외_응답을_반환한다() {
            // when & then
            assertThatCode(() -> adminHostUniversityService.getHostUniversity(999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.UNIVERSITY_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 생성 {

        @Test
        void 유효한_정보로_대학을_생성하면_성공한다() {
            // given
            Country country = countryFixture.미국();
            Region region = regionFixture.영미권();

            AdminHostUniversityCreateRequest request = new AdminHostUniversityCreateRequest(
                    "테스트 대학",
                    "Test University",
                    "테스트 대학",
                    "https://homepage.com",
                    "https://english-course.com",
                    "https://accommodation.com",
                    "https://logo.com/image.png",
                    "https://background.com/image.png",
                    "상세 정보",
                    country.getCode(),
                    region.getCode()
            );

            // when
            AdminHostUniversityDetailResponse response = adminHostUniversityService.createHostUniversity(request);

            // then
            assertThat(response.koreanName()).isEqualTo(request.koreanName());
            assertThat(response.englishName()).isEqualTo(request.englishName());

            HostUniversity savedUniversity = hostUniversityRepository.findById(response.id()).orElseThrow();
            assertThat(savedUniversity.getKoreanName()).isEqualTo(request.koreanName());
        }

        @Test
        void 이미_존재하는_한글명으로_생성하면_예외_응답을_반환한다() {
            // given
            HostUniversity existing = universityFixture.괌_대학();
            Country country = countryFixture.미국();
            Region region = regionFixture.영미권();

            AdminHostUniversityCreateRequest request = new AdminHostUniversityCreateRequest(
                    existing.getKoreanName(),
                    "New English Name",
                    "표시명",
                    null, null, null,
                    "https://logo.com/image.png",
                    "https://background.com/image.png",
                    null,
                    country.getCode(),
                    region.getCode()
            );

            // when & then
            assertThatCode(() -> adminHostUniversityService.createHostUniversity(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.HOST_UNIVERSITY_ALREADY_EXISTS.getMessage());
        }
    }

    @Nested
    class 수정 {

        @Test
        void 유효한_정보로_대학을_수정하면_성공한다() {
            // given
            HostUniversity university = universityFixture.괌_대학();
            Country country = countryFixture.일본();
            Region region = regionFixture.아시아();

            AdminHostUniversityUpdateRequest request = new AdminHostUniversityUpdateRequest(
                    "수정된 대학명",
                    "Updated University",
                    "수정된 표시명",
                    "https://new-homepage.com",
                    null, null,
                    "https://new-logo.com/image.png",
                    "https://new-background.com/image.png",
                    "수정된 상세 정보",
                    country.getCode(),
                    region.getCode()
            );

            // when
            AdminHostUniversityDetailResponse response = adminHostUniversityService.updateHostUniversity(
                    university.getId(), request);

            // then
            assertThat(response.koreanName()).isEqualTo(request.koreanName());
            assertThat(response.countryCode()).isEqualTo(country.getCode());

            HostUniversity updatedUniversity = hostUniversityRepository.findById(university.getId()).orElseThrow();
            assertThat(updatedUniversity.getKoreanName()).isEqualTo(request.koreanName());
        }

        @Test
        void 존재하지_않는_대학을_수정하면_예외_응답을_반환한다() {
            // given
            Country country = countryFixture.미국();
            Region region = regionFixture.영미권();

            AdminHostUniversityUpdateRequest request = new AdminHostUniversityUpdateRequest(
                    "수정된 대학명",
                    "Updated University",
                    "수정된 표시명",
                    null, null, null,
                    "https://logo.com/image.png",
                    "https://background.com/image.png",
                    null,
                    country.getCode(),
                    region.getCode()
            );

            // when & then
            assertThatCode(() -> adminHostUniversityService.updateHostUniversity(999L, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.UNIVERSITY_NOT_FOUND.getMessage());
        }

        @Test
        void 다른_대학의_한글명으로_수정하면_예외_응답을_반환한다() {
            // given
            HostUniversity university1 = universityFixture.괌_대학();
            HostUniversity university2 = universityFixture.메이지_대학();

            AdminHostUniversityUpdateRequest request = new AdminHostUniversityUpdateRequest(
                    university2.getKoreanName(),
                    "Updated University",
                    "수정된 표시명",
                    null, null, null,
                    "https://logo.com/image.png",
                    "https://background.com/image.png",
                    null,
                    university1.getCountry().getCode(),
                    university1.getRegion().getCode()
            );

            // when & then
            assertThatCode(() -> adminHostUniversityService.updateHostUniversity(university1.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.HOST_UNIVERSITY_ALREADY_EXISTS.getMessage());
        }

        @Test
        void 같은_대학의_한글명으로_수정하면_성공한다() {
            // given
            HostUniversity university = universityFixture.괌_대학();

            AdminHostUniversityUpdateRequest request = new AdminHostUniversityUpdateRequest(
                    university.getKoreanName(),
                    "Updated English Name",
                    "수정된 표시명",
                    null, null, null,
                    "https://logo.com/image.png",
                    "https://background.com/image.png",
                    null,
                    university.getCountry().getCode(),
                    university.getRegion().getCode()
            );

            // when
            AdminHostUniversityDetailResponse response = adminHostUniversityService.updateHostUniversity(
                    university.getId(), request);

            // then
            assertThat(response.koreanName()).isEqualTo(university.getKoreanName());
            assertThat(response.englishName()).isEqualTo(request.englishName());
        }
    }

    @Nested
    class 삭제 {

        @Test
        void 존재하는_대학을_삭제하면_성공한다() {
            // given
            HostUniversity university = universityFixture.괌_대학();

            // when
            adminHostUniversityService.deleteHostUniversity(university.getId());

            // then
            assertThat(hostUniversityRepository.findById(university.getId())).isEmpty();
        }

        @Test
        void 존재하지_않는_대학을_삭제하면_예외_응답을_반환한다() {
            // when & then
            assertThatCode(() -> adminHostUniversityService.deleteHostUniversity(999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.UNIVERSITY_NOT_FOUND.getMessage());
        }

        @Test
        void 참조하는_대학_지원_정보가_있으면_예외_응답을_반환한다() {
            // given
            HostUniversity university = universityFixture.괌_대학();
            univApplyInfoFixtureBuilder.univApplyInfo()
                    .termId(1L)
                    .koreanName("괌 대학 지원 정보")
                    .university(university)
                    .create();

            // when & then
            assertThatCode(() -> adminHostUniversityService.deleteHostUniversity(university.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.HOST_UNIVERSITY_HAS_REFERENCES.getMessage());
        }
    }
}
