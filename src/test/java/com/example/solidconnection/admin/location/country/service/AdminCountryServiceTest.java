package com.example.solidconnection.admin.location.country.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.admin.location.country.dto.AdminCountryCreateRequest;
import com.example.solidconnection.admin.location.country.dto.AdminCountryResponse;
import com.example.solidconnection.admin.location.country.dto.AdminCountryUpdateRequest;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.location.country.domain.Country;
import com.example.solidconnection.location.country.fixture.CountryFixture;
import com.example.solidconnection.location.country.repository.CountryRepository;
import com.example.solidconnection.location.region.domain.Region;
import com.example.solidconnection.location.region.fixture.RegionFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("국가 관련 관리자 서비스 테스트")
class AdminCountryServiceTest {

    @Autowired
    private AdminCountryService adminCountryService;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CountryFixture countryFixture;

    @Autowired
    private RegionFixture regionFixture;

    @Nested
    class 전체_국가_조회 {

        @Test
        void 국가가_없으면_빈_목록을_반환한다() {
            // when
            List<AdminCountryResponse> responses = adminCountryService.getAllCountries();

            // then
            assertThat(responses).isEqualTo(List.of());
        }

        @Test
        void 저장된_모든_국가를_조회한다() {
            // given
            Country country1 = countryFixture.미국();
            Country country2 = countryFixture.캐나다();
            Country country3 = countryFixture.일본();

            // when
            List<AdminCountryResponse> responses = adminCountryService.getAllCountries();

            // then
            assertThat(responses)
                    .hasSize(3)
                    .extracting(AdminCountryResponse::code)
                    .containsExactlyInAnyOrder(
                            country1.getCode(),
                            country2.getCode(),
                            country3.getCode()
                    );
        }
    }

    @Nested
    class 국가_생성 {

        @Test
        void 유효한_정보로_국가를_생성하면_성공한다() {
            // given
            Region region = regionFixture.아시아();
            AdminCountryCreateRequest request = new AdminCountryCreateRequest("KR", "대한민국", region.getCode());

            // when
            AdminCountryResponse response = adminCountryService.createCountry(request);

            // then
            assertThat(response.code()).isEqualTo("KR");
            assertThat(response.koreanName()).isEqualTo("대한민국");
            assertThat(response.regionCode()).isEqualTo(region.getCode());

            // 데이터베이스에 저장되었는지 확인
            Country savedCountry = countryRepository.findByCode(request.code()).orElseThrow();
            assertAll(
                    () -> assertThat(savedCountry.getKoreanName()).isEqualTo(request.koreanName()),
                    () -> assertThat(savedCountry.getRegionCode()).isEqualTo(request.regionCode())
            );
        }

        @Test
        void 이미_존재하는_코드로_국가를_생성하면_예외_응답을_반환한다() {
            // given
            Country country = countryFixture.미국();

            AdminCountryCreateRequest request = new AdminCountryCreateRequest("US", "새로운 미국", country.getRegionCode());

            // when & then
            assertThatCode(() -> adminCountryService.createCountry(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.COUNTRY_ALREADY_EXISTS.getMessage());
        }

        @Test
        void 이미_존재하는_한글명으로_국가를_생성하면_예외_응답을_반환한다() {
            // given
            countryFixture.일본();
            Region region = regionFixture.아시아();

            AdminCountryCreateRequest request = new AdminCountryCreateRequest("NEW_CODE", "일본", region.getCode());

            // when & then
            assertThatCode(() -> adminCountryService.createCountry(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.COUNTRY_ALREADY_EXISTS.getMessage());
        }

        @Test
        void 존재하지_않는_지역_코드로_국가를_생성하면_예외_응답을_반환한다() {
            // given
            AdminCountryCreateRequest request = new AdminCountryCreateRequest("KR", "대한민국", "NOT_EXIST_REGION");

            // when & then
            assertThatCode(() -> adminCountryService.createCountry(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.REGION_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 국가_수정 {

        @Test
        void 유효한_정보로_국가를_수정하면_성공한다() {
            // given
            Country country = countryFixture.미국();
            Region newRegion = regionFixture.유럽();

            AdminCountryUpdateRequest request = new AdminCountryUpdateRequest("미합중국", newRegion.getCode());

            // when
            AdminCountryResponse response = adminCountryService.updateCountry(country.getCode(), request);

            // then
            Country updatedCountry = countryRepository.findByCode(country.getCode()).orElseThrow();
            assertAll(
                    () -> assertThat(response.code()).isEqualTo(country.getCode()),
                    () -> assertThat(updatedCountry.getKoreanName()).isEqualTo(request.koreanName()),
                    () -> assertThat(updatedCountry.getRegionCode()).isEqualTo(request.regionCode())
            );
        }

        @Test
        void 존재하지_않는_국가_코드로_수정하면_예외_응답을_반환한다() {
            // given
            Region region = regionFixture.아시아();
            AdminCountryUpdateRequest request = new AdminCountryUpdateRequest("대한민국", region.getCode());

            // when & then
            assertThatCode(() -> adminCountryService.updateCountry("NOT_EXIST", request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.COUNTRY_NOT_FOUND.getMessage());
        }

        @Test
        void 다른_국가의_한글명으로_수정하면_예외_응답을_반환한다() {
            // given
            Country country1 = countryFixture.미국();
            Country country2 = countryFixture.캐나다();

            AdminCountryUpdateRequest request = new AdminCountryUpdateRequest(country2.getKoreanName(), country1.getRegionCode());

            // when & then
            assertThatCode(() -> adminCountryService.updateCountry(country1.getCode(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.COUNTRY_ALREADY_EXISTS.getMessage());
        }

        @Test
        void 같은_국가의_한글명으로_수정하면_성공한다() {
            // given
            Country country = countryFixture.일본();

            AdminCountryUpdateRequest request = new AdminCountryUpdateRequest(country.getKoreanName(), country.getRegionCode());

            // when
            AdminCountryResponse response = adminCountryService.updateCountry(country.getCode(), request);

            // then
            assertThat(response.code()).isEqualTo(country.getCode());
        }

        @Test
        void 존재하지_않는_지역_코드로_수정하면_예외_응답을_반환한다() {
            // given
            Country country = countryFixture.미국();

            AdminCountryUpdateRequest request = new AdminCountryUpdateRequest("미합중국", "NOT_EXIST_REGION");

            // when & then
            assertThatCode(() -> adminCountryService.updateCountry(country.getCode(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.REGION_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 국가_삭제 {

        @Test
        void 존재하는_국가를_삭제하면_성공한다() {
            // given
            Country country = countryFixture.미국();

            // when
            adminCountryService.deleteCountry(country.getCode());

            // then
            assertThat(countryRepository.findByCode(country.getCode())).isEmpty();
        }

        @Test
        void 존재하지_않는_국가를_삭제하면_예외_응답을_반환한다() {
            // when & then
            assertThatCode(() -> adminCountryService.deleteCountry("NOT_EXIST"))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.COUNTRY_NOT_FOUND.getMessage());
        }
    }
}
