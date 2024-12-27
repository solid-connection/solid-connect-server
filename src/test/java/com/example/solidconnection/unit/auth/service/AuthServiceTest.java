package com.example.solidconnection.unit.auth.service;

import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.auth.service.AuthService;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("인증 서비스 테스트")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private TokenService tokenService;

    @Mock
    private SiteUserRepository siteUserRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private static final String TEST_ACCESS_TOKEN = "testAccessToken";
    private static final String TEST_REFRESH_TOKEN = "testRefreshToken";
    private static final String SIGN_OUT_VALUE = "signOut";

    private SiteUser testUser;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
    }

    @Test
    @DisplayName("로그아웃_요청시_리프레시_토큰을_무효화한다()")
    void shouldInvalidateRefreshTokenWhenSignOut() {
        // given
        String refreshTokenKey = TokenType.REFRESH.addTokenPrefixToSubject(testUser.getEmail());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        authService.signOut(testUser.getEmail());

        // then & verify
        verify(valueOperations).set(
                eq(refreshTokenKey),
                eq(SIGN_OUT_VALUE),
                eq(604800000L),
                eq(TimeUnit.MILLISECONDS)
        );
    }


    @Test
    @DisplayName("회원탈퇴_요청시_탈퇴일자를_설정한다()")
    void shouldSetQuitedAtWhenUserQuits() {
        // given
        when(siteUserRepository.getByEmail(testUser.getEmail())).thenReturn(testUser);

        // when
        authService.quit(testUser.getEmail());

        // then
        assertThat(testUser.getQuitedAt()).isNotNull();
        assertThat(testUser.getQuitedAt()).isEqualTo(LocalDate.now().plusDays(1));

        // verify
        verify(siteUserRepository).getByEmail(testUser.getEmail());
    }

    @Test
    @DisplayName("존재하지_않는_이메일로_회원탈퇴_요청시_예외를_반환한다()")
    void shouldThrowExceptionWhenQuitWithNonExistentEmail() {
        // given
        when(siteUserRepository.getByEmail(testUser.getEmail()))
                .thenThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> authService.quit(testUser.getEmail()));
        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_NOT_FOUND.getCode());

        // verify
        verify(siteUserRepository).getByEmail(testUser.getEmail());
    }

    @Test
    @DisplayName("유효한_리프레시_토큰으로_재발급_요청시_새로운_액세스_토큰을_반환한다()")
    void shouldReturnNewAccessTokenWhenRefreshTokenValid() {
        // given
        String refreshTokenKey = TokenType.REFRESH.addTokenPrefixToSubject(testUser.getEmail());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(refreshTokenKey)).thenReturn(TEST_REFRESH_TOKEN);
        when(tokenService.generateToken(testUser.getEmail(), TokenType.ACCESS)).thenReturn(TEST_ACCESS_TOKEN);

        // when
        ReissueResponse response = authService.reissue(testUser.getEmail());

        // then
        assertThat(response.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);

        // verify
        verify(valueOperations).get(refreshTokenKey);
        verify(tokenService).generateToken(testUser.getEmail(), TokenType.ACCESS);
        verify(tokenService).saveToken(TEST_ACCESS_TOKEN, TokenType.ACCESS);
    }

    @Test
    @DisplayName("만료된_리프레시_토큰으로_재발급_요청시_예외를_반환한다()")
    void shouldThrowExceptionWhenRefreshTokenExpired() {
        // given
        String refreshTokenKey = TokenType.REFRESH.addTokenPrefixToSubject(testUser.getEmail());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(refreshTokenKey)).thenReturn(null);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> authService.reissue(testUser.getEmail()));
        assertThat(exception.getCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED.getCode());

        // verify
        verify(valueOperations).get(refreshTokenKey);
        verify(tokenService, never()).generateToken(any(), any());
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
}