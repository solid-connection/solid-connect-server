package com.example.solidconnection.custom.security.authentication;

import com.example.solidconnection.custom.security.userdetails.SiteUserDetails;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.domain.PreparationStatus;
import com.example.solidconnection.siteuser.domain.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SiteUserAuthenticationTest {

    @Test
    void 인증_정보에_저장된_토큰을_반환한다() {
        // given
        String token = "token";
        SiteUserAuthentication authentication = new SiteUserAuthentication(token);

        // when
        String result = authentication.getToken();

        // then
        assertThat(result).isEqualTo(token);
    }

    @Test
    void 인증_정보에_저장된_사용자를_반환한다() {
        // given
        SiteUserDetails userDetails = new SiteUserDetails(createSiteUser());
        SiteUserAuthentication authentication = new SiteUserAuthentication("token", userDetails);

        // when & then
        SiteUserDetails actual = (SiteUserDetails) authentication.getPrincipal();

        // then
        assertThat(actual)
                .extracting("siteUser")
                .extracting("id")
                .isEqualTo(userDetails.getSiteUser().getId());
    }

    @Test
    void 인증_전에_생성되면_isAuthenticated_는_false_를_반환한다() {
        // given
        SiteUserAuthentication authentication = new SiteUserAuthentication("token");

        // when & then
        assertThat(authentication.isAuthenticated()).isFalse();
    }

    @Test
    void 인증_후에_생성되면_isAuthenticated_는_true_를_반환한다() {
        // given
        SiteUserDetails userDetails = new SiteUserDetails(createSiteUser());
        SiteUserAuthentication authentication = new SiteUserAuthentication("token", userDetails);

        // when & then
        assertThat(authentication.isAuthenticated()).isTrue();
    }

    private SiteUser createSiteUser() {
        return new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                PreparationStatus.CONSIDERING,
                Role.MENTEE
        );
    }
}
