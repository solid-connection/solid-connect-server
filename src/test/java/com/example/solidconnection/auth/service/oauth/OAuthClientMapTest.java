package com.example.solidconnection.auth.service.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.siteuser.domain.AuthType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OAuthClientMap 테스트")
class OAuthClientMapTest {

    @Test
    void AuthType에_해당하는_Client를_반환한다() {
        // given
        OAuthClient appleClient = mock(OAuthClient.class);
        OAuthClient kakaoClient = mock(OAuthClient.class);
        given(appleClient.getAuthType()).willReturn(AuthType.APPLE);
        given(kakaoClient.getAuthType()).willReturn(AuthType.KAKAO);

        OAuthClientMap oAuthClientMap = new OAuthClientMap(
                List.of(appleClient, kakaoClient)
        );

        // when & then
        assertAll(
                () -> assertThat(oAuthClientMap.getOAuthClient(AuthType.APPLE)).isEqualTo(appleClient),
                () -> assertThat(oAuthClientMap.getOAuthClient(AuthType.KAKAO)).isEqualTo(kakaoClient)
        );
    }

    @Test
    void AuthType에_매칭되는_Client가_없으면_예외가_발생한다() {
        // given
        OAuthClient appleClient = mock(OAuthClient.class);
        given(appleClient.getAuthType()).willReturn(AuthType.APPLE);

        OAuthClientMap oAuthClientMap = new OAuthClientMap(
                List.of(appleClient)
        );

        // when & then
        assertThatCode(() -> oAuthClientMap.getOAuthClient(AuthType.KAKAO))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.NOT_DEFINED_ERROR.getMessage());
    }
}
