package com.example.solidconnection.security.aspect;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.security.annotation.RequireRoleAccess;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.common.exception.ErrorCode.ACCESS_DENIED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestContainerSpringBootTest
@DisplayName("권한 검사 Aspect 테스트")
class RoleAuthorizationAspectTest {

    @Autowired
    private TestService testService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Test
    void 요구하는_역할을_가진_사용자는_메서드를_정상적으로_호출할_수_있다() {
        // given
        SiteUser admin  = siteUserFixture.관리자();
        SiteUser mentor = siteUserFixture.멘토(1, "mentor");

        // when & then
        assertAll(
                () -> assertThatCode(() -> testService.adminOnlyMethod(admin))
                        .doesNotThrowAnyException(),
                () -> assertThatCode(() -> testService.mentorOrAdminMethod(mentor))
                        .doesNotThrowAnyException()
        );
    }

    @Test
    void 요구하는_역할이_없는_사용자가_메서드를_호출하면_예외가_발생한다() {
        // given
        SiteUser user = siteUserFixture.사용자();

        // when & then
        assertThatCode(() -> testService.mentorOrAdminMethod(user))
                .isInstanceOf(CustomException.class)
                .hasMessage(ACCESS_DENIED.getMessage());
    }

    @Test
    void 역할을_요구하지_않는_메서드는_누구나_호출할_수_있다() {
        // given
        SiteUser admin  = siteUserFixture.관리자();
        SiteUser mentor = siteUserFixture.멘토(1, "mentor");
        SiteUser user   = siteUserFixture.사용자();

        // when & then
        assertAll(
                () -> assertThatCode(() -> testService.publicMethod(admin))
                        .doesNotThrowAnyException(),
                () -> assertThatCode(() -> testService.publicMethod(mentor))
                        .doesNotThrowAnyException(),
                () -> assertThatCode(() -> testService.publicMethod(user))
                        .doesNotThrowAnyException()
        );
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestService testService() {
            return new TestService();
        }
    }

    @Component
    static class TestService {

        @RequireRoleAccess(roles = {Role.ADMIN})
        public boolean adminOnlyMethod(SiteUser siteUser) {
            return true;
        }

        @RequireRoleAccess(roles = {Role.ADMIN, Role.MENTOR})
        public boolean mentorOrAdminMethod(SiteUser siteUser) {
            return true;
        }

        public boolean publicMethod(SiteUser siteUser) {
            return true;
        }
    }
}
