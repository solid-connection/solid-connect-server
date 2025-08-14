package com.example.solidconnection.auth.service.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.example.solidconnection.auth.dto.oauth.OAuthCodeRequest;
import com.example.solidconnection.auth.dto.oauth.OAuthResponse;
import com.example.solidconnection.auth.dto.oauth.OAuthSignInResponse;
import com.example.solidconnection.auth.dto.oauth.OAuthUserInfoDto;
import com.example.solidconnection.auth.dto.oauth.SignUpPrepareResponse;
import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

@DisplayName("OAuth 서비스 테스트")
@TestContainerSpringBootTest
class OAuthServiceTest {

    @Autowired
    private OAuthService oAuthService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @MockBean
    private OAuthClientMap oauthClientMap;

    private final AuthType authType = AuthType.KAKAO;
    private final String oauthCode = "code";
    private final String email = "test@test.com";
    private final String profileImageUrl = "profile.jpg";
    private final String nickname = "testUser";

    @BeforeEach
    void setUp() { // 실제 client 호출하지 않도록 mocking
        OAuthUserInfoDto oauthUserInfoDto = mock(OAuthUserInfoDto.class);
        given(oauthUserInfoDto.getEmail()).willReturn(email);
        given(oauthUserInfoDto.getProfileImageUrl()).willReturn(profileImageUrl);
        given(oauthUserInfoDto.getNickname()).willReturn(nickname);

        OAuthClient oAuthClient = mock(OAuthClient.class);
        given(oauthClientMap.getOAuthClient(authType)).willReturn(oAuthClient);
        given(oAuthClient.getAuthType()).willReturn(authType);
        given(oAuthClient.getUserInfo(oauthCode)).willReturn(oauthUserInfoDto);
    }

    @Test
    void 기존_회원이라면_로그인한다() {
        // given
        siteUserFixture.사용자(email, authType);

        // when
        OAuthResponse response = oAuthService.processOAuth(authType, new OAuthCodeRequest(oauthCode));

        // then
        assertThat(response).isInstanceOf(OAuthSignInResponse.class);
        OAuthSignInResponse signInResponse = (OAuthSignInResponse) response;
        assertAll(
                () -> assertThat(signInResponse.isRegistered()).isTrue(),
                () -> assertThat(signInResponse.accessToken()).isNotBlank(),
                () -> assertThat(signInResponse.refreshToken()).isNotBlank()
        );
    }

    @Test
    void 신규_회원이라면_회원가입에_필요한_정보를_응답한다() {
        // when
        OAuthResponse response = oAuthService.processOAuth(authType, new OAuthCodeRequest(oauthCode));

        // then
        assertThat(response).isInstanceOf(SignUpPrepareResponse.class);
        SignUpPrepareResponse signUpPrepareResponse = (SignUpPrepareResponse) response;
        assertAll(
                () -> assertThat(signUpPrepareResponse.isRegistered()).isFalse(),
                () -> assertThat(signUpPrepareResponse.signUpToken()).isNotBlank(),
                () -> assertThat(signUpPrepareResponse.email()).isEqualTo(email),
                () -> assertThat(signUpPrepareResponse.profileImageUrl()).isEqualTo(profileImageUrl),
                () -> assertThat(signUpPrepareResponse.nickname()).isEqualTo(nickname)
        );
    }
}
