package com.example.solidconnection.custom.security.userdetails;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("사용자 인증 정보 테스트")
@TestContainerSpringBootTest
class SiteUserDetailsTest {

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Test
    void 사용자_권한을_정상적으로_반환한다() {
        // given
        SiteUser 테스트_유저 = siteUserFixture.테스트_유저();
        SiteUserDetails siteUserDetails = new SiteUserDetails(테스트_유저);

        // when
        Collection<? extends GrantedAuthority> authorities = siteUserDetails.getAuthorities();

        // then
        assertThat(authorities)
                .extracting("authority")
                .containsExactly("ROLE_" + 테스트_유저.getRole().name());
    }
}
