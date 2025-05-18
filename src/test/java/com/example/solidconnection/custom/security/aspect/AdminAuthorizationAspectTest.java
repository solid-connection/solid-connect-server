package com.example.solidconnection.custom.security.aspect;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.security.annotation.RequireAdminAccess;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.custom.exception.ErrorCode.ACCESS_DENIED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@TestContainerSpringBootTest
@DisplayName("어드민 권한 검사 Aspect 테스트")
class AdminAuthorizationAspectTest {

    @Autowired
    private TestService testService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Test
    void 어드민_사용자는_어드민_전용_메소드에_접근할_수_있다() {
        // given
        SiteUser 테스트_어드민 = siteUserFixture.테스트_어드민();

        // when
        boolean response = testService.adminOnlyMethod(테스트_어드민);

        // then
        assertThat(response).isTrue();
    }

    @Test
    void 일반_사용자가_어드민_전용_메소드에_접근하면_예외_응답을_반환한다() {
        // given
        SiteUser user = siteUserFixture.사용자();

        // when & then
        assertThatCode(() -> testService.adminOnlyMethod(user))
                .isInstanceOf(CustomException.class)
                .hasMessage(ACCESS_DENIED.getMessage());
    }

    @Test
    void 어드민_어노테이션이_없는_메소드는_모두_접근_가능하다() {
        // given
        SiteUser user = siteUserFixture.사용자();
        SiteUser 테스트_어드민 = siteUserFixture.테스트_어드민();

        // when
        boolean menteeResponse = testService.publicMethod(user);
        boolean adminResponse = testService.publicMethod(테스트_어드민);

        // then
        assertThat(menteeResponse).isTrue();
        assertThat(adminResponse).isTrue();
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

        @RequireAdminAccess
        public boolean adminOnlyMethod(SiteUser siteUser) {
            return true;
        }

        public boolean publicMethod(SiteUser siteUser) {
            return true;
        }
    }
}
