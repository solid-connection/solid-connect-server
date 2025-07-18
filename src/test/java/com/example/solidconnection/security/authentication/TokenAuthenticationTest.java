package com.example.solidconnection.security.authentication;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.solidconnection.security.userdetails.SiteUserDetails;
import com.example.solidconnection.siteuser.domain.ExchangeStatus;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("토큰 인증 정보 테스트")
class TokenAuthenticationTest {

    @Nested
    class Authentication의_인증_정보를_반환한다 {

        @Test
        void 토큰을_반환한다() {
            // given
            String token = "token";
            TokenAuthentication authentication = new TokenAuthentication(token);

            // when
            String result = authentication.getToken();

            // then
            assertThat(result).isEqualTo(token);
        }

        @Test
        void 사용자_정보를_반환한다() {
            // given
            SiteUserDetails userDetails = new SiteUserDetails(createSiteUser());
            TokenAuthentication authentication = new TokenAuthentication("token", userDetails);

            // when & then
            SiteUserDetails actual = (SiteUserDetails) authentication.getPrincipal();

            // then
            assertThat(actual)
                    .extracting("siteUser")
                    .extracting("id")
                    .isEqualTo(userDetails.getSiteUser().getId());
        }
    }

    @Nested
    class Authentication의_인증_상태를_반환한다 {

        @Test
        void 증명_수단만_포함하여_생성하면_미인증_상태이다() {
            // given
            TokenAuthentication authentication = new TokenAuthentication("token");

            // when & then
            assertThat(authentication.isAuthenticated()).isFalse();
        }

        @Test
        void 사용자_정보와_함께_생성하면_인증된_상태이다() {
            // given
            SiteUserDetails userDetails = new SiteUserDetails(createSiteUser());
            TokenAuthentication authentication = new TokenAuthentication("token", userDetails);

            // when & then
            assertThat(authentication.isAuthenticated()).isTrue();
        }
    }

    private SiteUser createSiteUser() {
        return new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                ExchangeStatus.CONSIDERING,
                Role.MENTEE
        );
    }
}
