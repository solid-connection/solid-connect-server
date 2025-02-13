package com.example.solidconnection.custom.security.userdetails;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
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
    private SiteUserRepository siteUserRepository;

    @Test
    void 사용자_권한을_정상적으로_반환한다() {
        // given
        SiteUser siteUser = siteUserRepository.save(createSiteUser());
        SiteUserDetails siteUserDetails = new SiteUserDetails(siteUser);

        // when
        Collection<? extends GrantedAuthority> authorities = siteUserDetails.getAuthorities();

        // then
        assertThat(authorities)
                .extracting("authority")
                .containsExactly("ROLE_" + siteUser.getRole().name());
    }

    private SiteUser createSiteUser() {
        return new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
    }
}
