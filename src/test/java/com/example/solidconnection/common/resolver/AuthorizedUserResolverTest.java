package com.example.solidconnection.common.resolver;

import static com.example.solidconnection.common.exception.ErrorCode.AUTHENTICATION_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.security.authentication.TokenAuthentication;
import com.example.solidconnection.security.userdetails.SiteUserDetails;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@TestContainerSpringBootTest
@DisplayName("인증된 사용자 argument resolver 테스트")
class AuthorizedUserResolverTest {

    @Autowired
    private AuthorizedUserResolver authorizedUserResolver;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void security_context_에_저장된_인증된_사용자를_반환한다() {
        // given
        SiteUser user = siteUserFixture.사용자();
        Authentication authentication = createAuthenticationWithUser(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        MethodParameter parameter = getTestMethodParameter("method", Long.class);

        // when
        Long resolvedUserId = (Long) authorizedUserResolver.resolveArgument(parameter, null, null, null);

        // then
        assertAll(
                () -> assertThat(resolvedUserId).isNotNull(),
                () -> assertThat(resolvedUserId).isEqualTo(user.getId())
        );
    }

    @Nested
    class security_context_에_저장된_사용자가_없는_경우 {

        @Test
        void 파라미터가_원시값이면_예외가_발생한다() {
            // given
            MethodParameter primitiveTypeParameter = getTestMethodParameter("primitiveType", long.class);

            // when, then
            assertThatCode(() -> authorizedUserResolver.resolveArgument(primitiveTypeParameter, null, null, null))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(AUTHENTICATION_FAILED.getMessage());
        }

        @Test
        void required_가_true_이면_예외가_발생한다() {
            // given
            MethodParameter parameter = getTestMethodParameter("required", Long.class);

            // when, then
            assertThatCode(() -> authorizedUserResolver.resolveArgument(parameter, null, null, null))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(AUTHENTICATION_FAILED.getMessage());
        }

        @Test
        void required_가_false_이면_null_을_반환한다() {
            // given
            MethodParameter parameter = getTestMethodParameter("notRequired", Long.class);

            // when, then
            assertThat(
                    authorizedUserResolver.resolveArgument(parameter, null, null, null)
            ).isNull();
        }
    }

    private TokenAuthentication createAuthenticationWithUser(SiteUser siteUser) {
        SiteUserDetails userDetails = new SiteUserDetails(siteUser);
        return new TokenAuthentication("token", userDetails);
    }

    private MethodParameter getTestMethodParameter(String methodName, Class<?> parameterType) {
        // 테스트의 목적을 불분명히 만들 수 있는 throws 절을 제거하기 위해 uncheckedException 로 변환한다.
        try {
            Method method = TestController.class.getMethod(methodName, parameterType);
            return new MethodParameter(method, 0);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Method not found: " + methodName, e);
        }
    }

    static class TestController {

        public void method(@AuthorizedUser Long userId) {
        }

        public void primitiveType(@AuthorizedUser long userId) {
        }

        public void required(@AuthorizedUser(required = true) Long userId) {
        }

        public void notRequired(@AuthorizedUser(required = false) Long userId) {
        }
    }
}
