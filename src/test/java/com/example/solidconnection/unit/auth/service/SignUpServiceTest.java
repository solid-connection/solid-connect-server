package com.example.solidconnection.unit.auth.service;

import com.example.solidconnection.auth.dto.SignUpRequest;
import com.example.solidconnection.auth.dto.SignUpResponse;
import com.example.solidconnection.auth.service.SignUpService;
import com.example.solidconnection.config.token.TokenService;
import com.example.solidconnection.config.token.TokenType;
import com.example.solidconnection.config.token.TokenValidator;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.entity.Country;
import com.example.solidconnection.entity.Region;
import com.example.solidconnection.repositories.CountryRepository;
import com.example.solidconnection.repositories.InterestedCountyRepository;
import com.example.solidconnection.repositories.InterestedRegionRepository;
import com.example.solidconnection.repositories.RegionRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("회원가입 서비스 테스트")
class SignUpServiceTest {

    @InjectMocks
    private SignUpService signUpService;

    @Mock
    private SiteUserRepository siteUserRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private TokenValidator tokenValidator;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private InterestedRegionRepository interestedRegionRepository;

    @Mock
    private InterestedCountyRepository interestedCountryRepository;

    private static final String TEST_KAKAO_TOKEN = "testKakaoToken";
    private static final String TEST_ACCESS_TOKEN = "testAccessToken";
    private static final String TEST_REFRESH_TOKEN = "testRefreshToken";
    private static final String TEST_NICKNAME = "testNickname";

    private SignUpRequest validSignUpRequest;
    private SiteUser testUser;
    private Region testRegion;
    private Country testCountry;

    @BeforeEach
    void setUp() {
        validSignUpRequest = createValidSignUpRequest();
        testUser = createTestUser();
        testRegion = createTestRegion();
        testCountry = createTestCountry();
    }

    @Test
    @DisplayName("올바른_회원가입_요청시_회원가입에_성공하고_토큰을_반환한다()")
    void shouldSuccessfullySignUpAndReturnTokens() {
        // given
        when(tokenService.getEmail(TEST_KAKAO_TOKEN)).thenReturn(testUser.getEmail());
        when(siteUserRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(siteUserRepository.existsByNickname(TEST_NICKNAME)).thenReturn(false);
        when(siteUserRepository.save(any(SiteUser.class))).thenReturn(testUser);
        when(regionRepository.findByKoreanNames(any())).thenReturn(List.of(testRegion));
        when(countryRepository.findByKoreanNames(any())).thenReturn(List.of(testCountry));
        when(tokenService.generateToken(testUser.getEmail(), TokenType.ACCESS)).thenReturn(TEST_ACCESS_TOKEN);
        when(tokenService.generateToken(testUser.getEmail(), TokenType.REFRESH)).thenReturn(TEST_REFRESH_TOKEN);

        // when
        SignUpResponse response = signUpService.signUp(validSignUpRequest);

        // then
        assertThat(response.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(response.refreshToken()).isEqualTo(TEST_REFRESH_TOKEN);

        // verify
        verify(tokenValidator).validateKakaoToken(TEST_KAKAO_TOKEN);
        verify(siteUserRepository).save(any(SiteUser.class));
        verify(interestedRegionRepository).saveAll(any());
        verify(interestedCountryRepository).saveAll(any());
        verify(tokenService).saveToken(TEST_REFRESH_TOKEN, TokenType.REFRESH);
    }

    @Test
    @DisplayName("이미_사용중인_닉네임으로_회원가입_시도시_예외를_반환한다()")
    void shouldThrowExceptionWhenNicknameAlreadyExists() {
        // given
        when(tokenService.getEmail(TEST_KAKAO_TOKEN)).thenReturn(testUser.getEmail());
        when(siteUserRepository.existsByNickname(TEST_NICKNAME)).thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> signUpService.signUp(validSignUpRequest));
        assertThat(exception.getCode()).isEqualTo(ErrorCode.NICKNAME_ALREADY_EXISTED.getCode());

        // verify
        verify(tokenValidator).validateKakaoToken(TEST_KAKAO_TOKEN);
        verify(siteUserRepository).existsByNickname(TEST_NICKNAME);
        verify(siteUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미_가입된_이메일로_회원가입_시도시_예외를_반환한다()")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // given
        when(tokenService.getEmail(TEST_KAKAO_TOKEN)).thenReturn(testUser.getEmail());
        when(siteUserRepository.existsByNickname(TEST_NICKNAME)).thenReturn(false);
        when(siteUserRepository.existsByEmail(testUser.getEmail())).thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> signUpService.signUp(validSignUpRequest));
        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_ALREADY_EXISTED.getCode());

        // verify
        verify(siteUserRepository).existsByEmail(testUser.getEmail());
        verify(siteUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("만료된_카카오_토큰으로_회원가입_시도시_예외를_반환한다()")
    void shouldThrowExceptionWhenKakaoTokenExpired() {
        // given
        doThrow(new CustomException(ErrorCode.INVALID_SERVICE_PUBLISHED_KAKAO_TOKEN))
                .when(tokenValidator).validateKakaoToken(TEST_KAKAO_TOKEN);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> signUpService.signUp(validSignUpRequest));
        assertThat(exception.getCode()).isEqualTo(ErrorCode.INVALID_SERVICE_PUBLISHED_KAKAO_TOKEN.getCode());

        // verify
        verify(tokenValidator).validateKakaoToken(TEST_KAKAO_TOKEN);
        verify(siteUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미_사용된_카카오_토큰으로_회원가입_시도시_예외를_반환한다()")
    void shouldThrowExceptionWhenKakaoTokenAlreadyUsed() {
        // given
        doThrow(new CustomException(ErrorCode.INVALID_SERVICE_PUBLISHED_KAKAO_TOKEN))
                .when(tokenValidator).validateKakaoToken(TEST_KAKAO_TOKEN);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> signUpService.signUp(validSignUpRequest));
        assertThat(exception.getCode()).isEqualTo(ErrorCode.INVALID_SERVICE_PUBLISHED_KAKAO_TOKEN.getCode());

        // verify
        verify(tokenValidator).validateKakaoToken(TEST_KAKAO_TOKEN);
        verify(siteUserRepository, never()).save(any());
    }

    private SignUpRequest createValidSignUpRequest() {
        return new SignUpRequest(
                TEST_KAKAO_TOKEN,
                List.of("미주권"),
                List.of("브라질"),
                PreparationStatus.CONSIDERING,
                "https://example.com/profile.jpg",
                Gender.PREFER_NOT_TO_SAY,
                TEST_NICKNAME,
                "1999-10-21"
        );
    }

    private SiteUser createTestUser() {
        return new SiteUser(
                "test@example.com",
                TEST_NICKNAME,
                "https://example.com/profile.jpg",
                "1999-10-21",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.PREFER_NOT_TO_SAY
        );
    }

    private Region createTestRegion() {
        return new Region("AMERICAS", "미주권");
    }

    private Country createTestCountry() {
        return new Country("BR", "브라질", testRegion);
    }
}