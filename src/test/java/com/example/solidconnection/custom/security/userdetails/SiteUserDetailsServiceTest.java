package com.example.solidconnection.custom.security.userdetails;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static com.example.solidconnection.custom.exception.ErrorCode.AUTHENTICATION_FAILED;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("사용자 인증 정보 서비스 테스트")
@TestContainerSpringBootTest
class SiteUserDetailsServiceTest {

    @Autowired
    private SiteUserDetailsService userDetailsService;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Test
    void 사용자_인증_정보를_반환한다() {
        // given
        SiteUser siteUser = siteUserRepository.save(createSiteUser());
        String username = getUserName(siteUser);

        // when
        SiteUserDetails userDetails = (SiteUserDetails) userDetailsService.loadUserByUsername(username);

        // then
        assertAll(
                () -> assertThat(userDetails.getUsername()).isEqualTo(username),
                () -> assertThat(userDetails.getSiteUser()).extracting("id").isEqualTo(siteUser.getId())
        );
    }

    @Test
    void 사용자_권한_정보를_정상적으로_반환한다() {
        // given
        SiteUser siteUser = siteUserRepository.save(createSiteUser());
        String username = getUserName(siteUser);

        // when
        SiteUserDetails userDetails = (SiteUserDetails) userDetailsService.loadUserByUsername(username);

        // then
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_" + siteUser.getRole().name());
    }

    @Nested
    class 예외_응답을_반환한다 {

        @Test
        void 지정되지_않은_형식의_식별자가_주어지면_예외_응답을_반환한다() {
            // given
            String username = "notNumber";

            // when & then
            assertThatCode(() -> userDetailsService.loadUserByUsername(username))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(INVALID_TOKEN.getMessage());
        }

        @Test
        void 식별자에_해당하는_사용자가_없으면_예외_응답을_반환한다() {
            // given
            String username = "1234";

            // when & then
            assertThatCode(() -> userDetailsService.loadUserByUsername(username))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(AUTHENTICATION_FAILED.getMessage());
        }

        @Test
        void 탈퇴한_사용자이면_예외_응답을_반환한다() {
            // given
            SiteUser siteUser = createSiteUser();
            siteUser.setQuitedAt(LocalDate.now());
            siteUserRepository.save(siteUser);
            String username = getUserName(siteUser);

            // when & then
            assertThatCode(() -> userDetailsService.loadUserByUsername(username))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(AUTHENTICATION_FAILED.getMessage());
        }
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

    private String getUserName(SiteUser siteUser) {
        return siteUser.getId().toString();
    }
}
