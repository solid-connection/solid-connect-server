package com.example.solidconnection.e2e;

import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.auth.dto.SignUpResponse;
import com.example.solidconnection.config.token.TokenService;
import com.example.solidconnection.config.token.TokenType;
import com.example.solidconnection.custom.response.DataResponse;
import com.example.solidconnection.entity.Country;
import com.example.solidconnection.entity.InterestedCountry;
import com.example.solidconnection.entity.InterestedRegion;
import com.example.solidconnection.entity.Region;
import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.repositories.CountryRepository;
import com.example.solidconnection.repositories.InterestedCountyRepository;
import com.example.solidconnection.repositories.InterestedRegionRepository;
import com.example.solidconnection.repositories.RegionRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("회원가입 테스트")
class SignUpTest extends BaseEndToEndTest {

    @Autowired
    SiteUserRepository siteUserRepository;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    RegionRepository regionRepository;

    @Autowired
    InterestedRegionRepository interestedRegionRepository;

    @Autowired
    InterestedCountyRepository interestedCountyRepository;

    @Autowired
    TokenService tokenService;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Test
    void 유효한_카카오_토큰으로_회원가입한다() {
        // 테스트를 위한 데이터 셋업
        Region region = regionRepository.save(new Region("EROUPE", "유럽"));
        List<Country> countries = countryRepository.saveAll(List.of(
                new Country("FR", "프랑스", region),
                new Country("DE", "독일", region)));

        // 카카오 토큰 발급
        String email = "email@email.com";
        String generatedKakaoToken = tokenService.generateToken(email, TokenType.KAKAO_OAUTH);
        tokenService.saveToken(generatedKakaoToken, TokenType.KAKAO_OAUTH);

        // request body 생성
        List<String> interestedRegionNames = List.of("유럽");
        List<String> interestedCountryNames = List.of("프랑스", "독일");
        SignUpRequest signUpRequest = new SignUpRequest(generatedKakaoToken, interestedRegionNames, interestedCountryNames,
                PreparationStatus.CONSIDERING, "nickname", "profile", Gender.FEMALE, "2000-01-01");

        SignUpResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signUpRequest)
                .when().post("/auth/sign-up")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<DataResponse<SignUpResponse>>() {
                })
                .data();

        SiteUser savedSiteUser = siteUserRepository.getByEmail(email);
        assertAll(
                "회원 정보를 저장한다.",
                () -> assertThat(savedSiteUser.getId()).isNotNull(),
                () -> assertThat(savedSiteUser.getEmail()).isEqualTo(email),
                () -> assertThat(savedSiteUser.getBirth()).isEqualTo(signUpRequest.birth()),
                () -> assertThat(savedSiteUser.getNickname()).isEqualTo(signUpRequest.nickname()),
                () -> assertThat(savedSiteUser.getProfileImageUrl()).isEqualTo(signUpRequest.profileImageUrl()),
                () -> assertThat(savedSiteUser.getGender()).isEqualTo(signUpRequest.gender()),
                () -> assertThat(savedSiteUser.getPreparationStage()).isEqualTo(signUpRequest.preparationStatus()));

        List<Region> interestedRegions = interestedRegionRepository.findAllBySiteUser(savedSiteUser).stream()
                .map(InterestedRegion::getRegion)
                .toList();
        List<Country> interestedCountries = interestedCountyRepository.findAllBySiteUser(savedSiteUser).stream()
                .map(InterestedCountry::getCountry)
                .toList();
        assertAll(
                "관심 지역과 나라 정보를 저장한다.",
                () -> assertThat(interestedRegions).containsExactlyInAnyOrder(region),
                () -> assertThat(interestedCountries).containsExactlyElementsOf(countries)
        );

        assertThat(redisTemplate.opsForValue().get(TokenType.REFRESH.createTokenKey(email)))
                .as("리프레시 토큰을 저장한다.")
                .isEqualTo(response.refreshToken());
    }
}
