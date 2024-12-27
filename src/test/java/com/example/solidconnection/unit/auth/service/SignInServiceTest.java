package com.example.solidconnection.unit.auth.service;

import com.example.solidconnection.auth.client.KakaoOAuthClient;
import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.auth.dto.kakao.FirstAccessResponse;
import com.example.solidconnection.auth.dto.kakao.KakaoCodeRequest;
import com.example.solidconnection.auth.dto.kakao.KakaoOauthResponse;
import com.example.solidconnection.auth.dto.kakao.KakaoUserInfoDto;
import com.example.solidconnection.auth.service.SignInService;
import com.example.solidconnection.config.token.TokenService;
import com.example.solidconnection.config.token.TokenType;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("카카오 로그인 서비스 테스트")
class SignInServiceTest {

    @InjectMocks
    private SignInService signInService;

    @Mock
    private SiteUserRepository siteUserRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private KakaoOAuthClient kakaoOAuthClient;

    private static final String TEST_ACCESS_TOKEN = "testAccessToken";
    private static final String TEST_REFRESH_TOKEN = "testRefreshToken";
    private static final String TEST_KAKAO_OAUTH_TOKEN = "testKakaoOauthToken";
    private static final String VALID_CODE = "validCode";
    private static final String INVALID_CODE = "invalidCode";

    private SiteUser testUser;
    private KakaoUserInfoDto testKakaoUserInfo;
    private KakaoCodeRequest validKakaoCodeRequest;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testKakaoUserInfo = createTestKakaoUserInfoDto();
        validKakaoCodeRequest = new KakaoCodeRequest(VALID_CODE);
    }

    @Test
    @DisplayName("기존_회원이_로그인하면_로그인_정보를_반환한다()")
    void shouldReturnSignInInfoWhenUserAlreadyRegistered() {
        // given
        when(kakaoOAuthClient.processOauth(VALID_CODE)).thenReturn(testKakaoUserInfo);
        when(siteUserRepository.existsByEmail(testUser.getEmail())).thenReturn(true);
        when(siteUserRepository.getByEmail(testUser.getEmail())).thenReturn(testUser);
        when(tokenService.generateToken(testUser.getEmail(), TokenType.ACCESS)).thenReturn(TEST_ACCESS_TOKEN);
        when(tokenService.generateToken(testUser.getEmail(), TokenType.REFRESH)).thenReturn(TEST_REFRESH_TOKEN);

        // when
        KakaoOauthResponse response = signInService.signIn(validKakaoCodeRequest);

        // then
        assertThat(response).isInstanceOf(SignInResponse.class);
        SignInResponse signInResponse = (SignInResponse) response;
        assertThat(signInResponse.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(signInResponse.refreshToken()).isEqualTo(TEST_REFRESH_TOKEN);
        assertThat(signInResponse.isRegistered()).isTrue();

        // verify
        verify(kakaoOAuthClient).processOauth(VALID_CODE);
        verify(siteUserRepository).existsByEmail(testUser.getEmail());
        verify(siteUserRepository).getByEmail(testUser.getEmail());
        verify(tokenService).generateToken(testUser.getEmail(), TokenType.ACCESS);
        verify(tokenService).generateToken(testUser.getEmail(), TokenType.REFRESH);
        verify(tokenService).saveToken(TEST_REFRESH_TOKEN, TokenType.REFRESH);
    }

    @Test
    @DisplayName("탈퇴한_회원이_로그인하면_탈퇴일자를_초기화하고_로그인_정보를_반환한다()")
    void shouldResetQuitedAtAndReturnSignInInfoWhenQuitedUserSignsIn() {
        // given
        testUser.setQuitedAt(LocalDate.now().minusDays(1));

        when(kakaoOAuthClient.processOauth(VALID_CODE)).thenReturn(testKakaoUserInfo);
        when(siteUserRepository.existsByEmail(testUser.getEmail())).thenReturn(true);
        when(siteUserRepository.getByEmail(testUser.getEmail())).thenReturn(testUser);
        when(tokenService.generateToken(testUser.getEmail(), TokenType.ACCESS)).thenReturn(TEST_ACCESS_TOKEN);
        when(tokenService.generateToken(testUser.getEmail(), TokenType.REFRESH)).thenReturn(TEST_REFRESH_TOKEN);

        // when
        KakaoOauthResponse response = signInService.signIn(validKakaoCodeRequest);

        // then
        assertThat(response).isInstanceOf(SignInResponse.class);
        assertThat(testUser.getQuitedAt()).isNull();

        // verify
        verify(siteUserRepository).getByEmail(testUser.getEmail());
    }

    @Test
    @DisplayName("신규_회원이_로그인하면_회원가입_정보를_반환한다()")
    void shouldReturnSignUpInfoWhenUserNotRegistered() {
        // given
        when(kakaoOAuthClient.processOauth(VALID_CODE)).thenReturn(testKakaoUserInfo);
        when(siteUserRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(tokenService.generateToken(testUser.getEmail(), TokenType.KAKAO_OAUTH)).thenReturn(TEST_KAKAO_OAUTH_TOKEN);

        // when
        KakaoOauthResponse response = signInService.signIn(validKakaoCodeRequest);

        // then
        assertThat(response).isInstanceOf(FirstAccessResponse.class);
        FirstAccessResponse firstAccessResponse = (FirstAccessResponse) response;
        assertThat(firstAccessResponse.kakaoOauthToken()).isEqualTo(TEST_KAKAO_OAUTH_TOKEN);
        assertThat(firstAccessResponse.isRegistered()).isFalse();
        assertThat(firstAccessResponse.email()).isEqualTo(testUser.getEmail());
        assertThat(firstAccessResponse.nickname()).isEqualTo("testNickname");
        assertThat(firstAccessResponse.profileImageUrl()).isEqualTo("testProfileImageUrl");

        // verify
        verify(siteUserRepository).existsByEmail(testUser.getEmail());
        verify(tokenService).generateToken(testUser.getEmail(), TokenType.KAKAO_OAUTH);
        verify(tokenService).saveToken(TEST_KAKAO_OAUTH_TOKEN, TokenType.KAKAO_OAUTH);
    }

    @Test
    @DisplayName("유효하지_않은_인증_코드로_로그인_시도하면_예외를_반환한다()")
    void shouldThrowExceptionWhenInvalidAuthCodeProvided() {
        // given
        KakaoCodeRequest invalidRequest = new KakaoCodeRequest(INVALID_CODE);
        when(kakaoOAuthClient.processOauth(INVALID_CODE))
                .thenThrow(new CustomException(ErrorCode.INVALID_OR_EXPIRED_KAKAO_AUTH_CODE));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> signInService.signIn(invalidRequest));
        assertThat(exception.getCode()).isEqualTo(ErrorCode.INVALID_OR_EXPIRED_KAKAO_AUTH_CODE.getCode());

        // verify
        verify(kakaoOAuthClient).processOauth(INVALID_CODE);
    }

    @Test
    @DisplayName("카카오_리다이렉트_URI가_일치하지_않으면_예외를_반환한다()")
    void shouldThrowExceptionWhenKakaoRedirectUriMismatch() {
        // given
        when(kakaoOAuthClient.processOauth(VALID_CODE))
                .thenThrow(new CustomException(ErrorCode.KAKAO_REDIRECT_URI_MISMATCH));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> signInService.signIn(validKakaoCodeRequest));
        assertThat(exception.getCode()).isEqualTo(ErrorCode.KAKAO_REDIRECT_URI_MISMATCH.getCode());

        // verify
        verify(kakaoOAuthClient).processOauth(VALID_CODE);
    }

    @Test
    @DisplayName("카카오_사용자_정보_조회에_실패하면_예외를_반환한다()")
    void shouldThrowExceptionWhenKakaoUserInfoFetchFails() {
        // given
        when(kakaoOAuthClient.processOauth(VALID_CODE))
                .thenThrow(new CustomException(ErrorCode.KAKAO_USER_INFO_FAIL));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> signInService.signIn(validKakaoCodeRequest));
        assertThat(exception.getCode()).isEqualTo(ErrorCode.KAKAO_USER_INFO_FAIL.getCode());

        // verify
        verify(kakaoOAuthClient).processOauth(VALID_CODE);
    }

    private SiteUser createTestUser() {
        return new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-10-21",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
    }

    private KakaoUserInfoDto createTestKakaoUserInfoDto() {
        KakaoUserInfoDto.KakaoAccountDto kakaoAccountDto = new KakaoUserInfoDto.KakaoAccountDto(
                new KakaoUserInfoDto.KakaoAccountDto.KakaoProfileDto("testProfileImageUrl", "testNickname"),
                testUser.getEmail());
        return new KakaoUserInfoDto(kakaoAccountDto);
    }
}