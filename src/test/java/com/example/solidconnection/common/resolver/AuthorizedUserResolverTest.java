package com.example.solidconnection.common.resolver;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.security.authentication.SiteUserAuthentication;
import com.example.solidconnection.security.userdetails.SiteUserDetails;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.example.solidconnection.common.exception.ErrorCode.AUTHENTICATION_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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

        MethodParameter parameter = mock(MethodParameter.class);
        AuthorizedUser authorizedUser = mock(AuthorizedUser.class);
        given(parameter.getParameterAnnotation(AuthorizedUser.class)).willReturn(authorizedUser);
        given(authorizedUser.required()).willReturn(false);

        // when
        SiteUser resolveSiteUser = (SiteUser) authorizedUserResolver.resolveArgument(parameter, null, null, null);

        // then
        assertThat(resolveSiteUser).isEqualTo(user);
    }

    @Nested
    class security_context_에_저장된_사용자가_없는_경우 {

        @Test
        void required_가_true_이면_예외_응답을_반환한다() {
            // given
            MethodParameter parameter = mock(MethodParameter.class);
            AuthorizedUser authorizedUser = mock(AuthorizedUser.class);
            given(parameter.getParameterAnnotation(AuthorizedUser.class)).willReturn(authorizedUser);
            given(authorizedUser.required()).willReturn(true);

            // when, then
            assertThatCode(() -> authorizedUserResolver.resolveArgument(parameter, null, null, null))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(AUTHENTICATION_FAILED.getMessage());
        }

        @Test
        void required_가_false_이면_null_을_반환한다() {
            // given
            MethodParameter parameter = mock(MethodParameter.class);
            AuthorizedUser authorizedUser = mock(AuthorizedUser.class);
            given(parameter.getParameterAnnotation(AuthorizedUser.class)).willReturn(authorizedUser);
            given(authorizedUser.required()).willReturn(false);

            // when, then
            assertThat(
                    authorizedUserResolver.resolveArgument(parameter, null, null, null)
            ).isNull();
        }
    }

    private SiteUserAuthentication createAuthenticationWithUser(SiteUser siteUser) {
        SiteUserDetails userDetails = new SiteUserDetails(siteUser);
        return new SiteUserAuthentication("token", userDetails);
    }
}
