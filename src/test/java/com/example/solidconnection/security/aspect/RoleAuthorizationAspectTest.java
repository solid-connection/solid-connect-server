package com.example.solidconnection.security.aspect;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.security.annotation.RequireRoleAccess;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.common.exception.ErrorCode.ACCESS_DENIED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@TestContainerSpringBootTest
@DisplayName("권한 검사 Aspect 테스트")
class RoleAuthorizationAspectTest {

    @Autowired
    private TestService testService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Nested
    class 어드민_권한_테스트 {

        @Test
        void 어드민은_어드민_전용_메소드에_접근할_수_있다() {
            // given
            SiteUser admin = siteUserFixture.관리자();

            // when
            boolean response = testService.adminOnlyMethod(admin);

            // then
            assertThat(response).isTrue();
        }

        @Test
        void 어드민은_멘토_또는_어드민_메소드에_접근할_수_있다() {
            // given
            SiteUser admin = siteUserFixture.관리자();

            // when
            boolean response = testService.mentorOrAdminMethod(admin);

            // then
            assertThat(response).isTrue();
        }

        @Test
        void 어드민은_권한_제한이_없는_메소드에_접근할_수_있다() {
            // given
            SiteUser admin = siteUserFixture.관리자();

            // when
            boolean response = testService.publicMethod(admin);

            // then
            assertThat(response).isTrue();
        }
    }

    @Nested
    class 멘토_권한_테스트 {

        @Test
        void 멘토가_어드민_전용_메소드에_접근하면_예외가_발생한다() {
            // given
            SiteUser mentor = siteUserFixture.멘토();

            // when & then
            assertThatCode(() -> testService.adminOnlyMethod(mentor))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ACCESS_DENIED.getMessage());
        }

        @Test
        void 멘토는_멘토_또는_어드민_메소드에_접근할_수_있다() {
            // given
            SiteUser mentor = siteUserFixture.멘토();

            // when
            boolean response = testService.mentorOrAdminMethod(mentor);

            // then
            assertThat(response).isTrue();
        }

        @Test
        void 멘토는_권한_제한이_없는_메소드에_접근할_수_있다() {
            // given
            SiteUser mentor = siteUserFixture.멘토();

            // when
            boolean response = testService.publicMethod(mentor);

            // then
            assertThat(response).isTrue();
        }
    }

    @Nested
    class 일반_사용자_권한_테스트 {

        @Test
        void 일반_사용자가_어드민_전용_메소드에_접근하면_예외가_발생한다() {
            // given
            SiteUser user = siteUserFixture.사용자();

            // when & then
            assertThatCode(() -> testService.adminOnlyMethod(user))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ACCESS_DENIED.getMessage());
        }

        @Test
        void 일반_사용자가_멘토_또는_어드민_메소드에_접근하면_예외가_발생한다() {
            // given
            SiteUser user = siteUserFixture.사용자();

            // when & then
            assertThatCode(() -> testService.mentorOrAdminMethod(user))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ACCESS_DENIED.getMessage());
        }

        @Test
        void 일반_사용자는_권한_제한이_없는_메소드에_접근할_수_있다() {
            // given
            SiteUser user = siteUserFixture.사용자();

            // when
            boolean response = testService.publicMethod(user);

            // then
            assertThat(response).isTrue();
        }
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
