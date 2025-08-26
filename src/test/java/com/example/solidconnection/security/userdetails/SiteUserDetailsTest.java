package com.example.solidconnection.security.userdetails;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.util.Collection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;

@DisplayName("사용자 인증 정보 테스트")
@TestContainerSpringBootTest
class SiteUserDetailsTest {

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Test
    void 사용자_권한을_정상적으로_반환한다() {
        // given
        SiteUser user = siteUserFixture.사용자();
        SiteUserDetails siteUserDetails = new SiteUserDetails(user);

        // when
        Collection<? extends GrantedAuthority> authorities = siteUserDetails.getAuthorities();

        // then
        assertThat(authorities)
                .extracting("authority")
                .containsExactly("ROLE_" + user.getRole().name());
    }
}
