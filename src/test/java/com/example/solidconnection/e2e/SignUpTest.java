package com.example.solidconnection.e2e;

import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.auth.service.AuthTokenProvider;
import com.example.solidconnection.auth.service.oauth.OAuthSignUpTokenProvider;
import com.example.solidconnection.custom.response.ErrorResponse;
import com.example.solidconnection.entity.Country;
import com.example.solidconnection.entity.InterestedCountry;
import com.example.solidconnection.entity.InterestedRegion;
import com.example.solidconnection.entity.Region;
import com.example.solidconnection.repositories.CountryRepository;
import com.example.solidconnection.repositories.InterestedCountyRepository;
import com.example.solidconnection.repositories.InterestedRegionRepository;
import com.example.solidconnection.repositories.RegionRepository;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.example.solidconnection.auth.domain.TokenType.REFRESH;
import static com.example.solidconnection.custom.exception.ErrorCode.NICKNAME_ALREADY_EXISTED;
import static com.example.solidconnection.custom.exception.ErrorCode.SIGN_UP_TOKEN_INVALID;
import static com.example.solidconnection.custom.exception.ErrorCode.USER_ALREADY_EXISTED;
import static com.example.solidconnection.e2e.DynamicFixture.createSiteUserByEmail;
import static com.example.solidconnection.e2e.DynamicFixture.createSiteUserByNickName;
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
    AuthTokenProvider authTokenProvider;

    @Autowired
    OAuthSignUpTokenProvider OAuthSignUpTokenProvider;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Test
    void 유효한_카카오_토큰으로_회원가입한다() {
        // setup - 국가, 지역 정보 저장
        Region region = regionRepository.save(new Region("EROUPE", "유럽"));
        List<Country> countries = countryRepository.saveAll(List.of(
                new Country("FR", "프랑스", region),
                new Country("DE", "독일", region)));

        // setup - 카카오 토큰 발급
        String email = "email@email.com";
        String generatedKakaoToken = OAuthSignUpTokenProvider.generateAndSaveSignUpToken(email, AuthType.KAKAO);

        // request - body 생성 및 요청
        List<String> interestedRegionNames = List.of("유럽");
        List<String> interestedCountryNames = List.of("프랑스", "독일");
        SignUpRequest signUpRequest = new SignUpRequest(generatedKakaoToken, interestedRegionNames, interestedCountryNames,
                PreparationStatus.CONSIDERING, "profile", Gender.FEMALE, "nickname", "2000-01-01");
        SignInResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signUpRequest)
                .when().post("/auth/sign-up")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(SignInResponse.class);

        SiteUser savedSiteUser = siteUserRepository.findByEmailAndAuthType(email, AuthType.KAKAO).get();
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
                () -> assertThat(interestedCountries).containsExactlyInAnyOrderElementsOf(countries)
        );

        assertThat(redisTemplate.opsForValue().get(REFRESH.addPrefix(savedSiteUser.getId().toString())))
                .as("리프레시 토큰을 저장한다.")
                .isEqualTo(response.refreshToken());
    }

    @Test
    void 이미_있는_닉네임으로_회원가입하면_예외를_응답한다() {
        // setup - 회원 정보 저장
        String alreadyExistNickname = "nickname";
        SiteUser alreadyExistUser = createSiteUserByNickName(alreadyExistNickname);
        siteUserRepository.save(alreadyExistUser);

        // setup - 카카오 토큰 발급
        String email = "test@email.com";
        String generatedKakaoToken = OAuthSignUpTokenProvider.generateAndSaveSignUpToken(email, AuthType.KAKAO);

        // request - body 생성 및 요청
        SignUpRequest signUpRequest = new SignUpRequest(generatedKakaoToken, null, null,
                PreparationStatus.CONSIDERING, "profile", Gender.FEMALE, alreadyExistNickname, "2000-01-01");
        ErrorResponse errorResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signUpRequest)
                .when().post("/auth/sign-up")
                .then().log().all()
                .statusCode(HttpStatus.CONFLICT.value())
                .extract().as(ErrorResponse.class);

        assertThat(errorResponse.message())
                .isEqualTo(NICKNAME_ALREADY_EXISTED.getMessage());
    }

    @Test
    void 이미_있는_이메일로_회원가입하면_예외를_응답한다() {
        // setup - 회원 정보 저장
        String alreadyExistEmail = "email@email.com";
        SiteUser alreadyExistUser = createSiteUserByEmail(alreadyExistEmail);
        siteUserRepository.save(alreadyExistUser);

        // setup - 카카오 토큰 발급
        String generatedKakaoToken = OAuthSignUpTokenProvider.generateAndSaveSignUpToken(alreadyExistEmail, AuthType.KAKAO);

        // request - body 생성 및 요청
        SignUpRequest signUpRequest = new SignUpRequest(generatedKakaoToken, null, null,
                PreparationStatus.CONSIDERING, "profile", Gender.FEMALE, "nickname0", "2000-01-01");
        ErrorResponse errorResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signUpRequest)
                .when().post("/auth/sign-up")
                .then().log().all()
                .statusCode(HttpStatus.CONFLICT.value())
                .extract().as(ErrorResponse.class);

        assertThat(errorResponse.message())
                .isEqualTo(USER_ALREADY_EXISTED.getMessage());
    }

    @Test
    void 유효하지_않은_카카오_토큰으로_회원가입을_하면_예외를_응답한다() {
        SignUpRequest signUpRequest = new SignUpRequest("invalid", null, null,
                PreparationStatus.CONSIDERING, "profile", Gender.FEMALE, "nickname", "2000-01-01");
        ErrorResponse errorResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signUpRequest)
                .when().post("/auth/sign-up")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().as(ErrorResponse.class);

        assertThat(errorResponse.message())
                .contains(SIGN_UP_TOKEN_INVALID.getMessage());
    }
}
