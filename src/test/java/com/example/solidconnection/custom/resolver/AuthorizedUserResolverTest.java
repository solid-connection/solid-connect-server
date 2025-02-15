package com.example.solidconnection.custom.resolver;


import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.security.authentication.SiteUserAuthentication;
import com.example.solidconnection.custom.security.userdetails.SiteUserDetails;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.example.solidconnection.custom.exception.ErrorCode.AUTHENTICATION_FAILED;
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
    private SiteUserRepository siteUserRepository;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void security_context_에_저장된_인증된_사용자를_반환한다() {
        // given
        SiteUser siteUser = createAndSaveSiteUser();
        Authentication authentication = createAuthenticationWithUser(siteUser);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        MethodParameter parameter = mock(MethodParameter.class);
        AuthorizedUser authorizedUser = mock(AuthorizedUser.class);
        given(parameter.getParameterAnnotation(AuthorizedUser.class)).willReturn(authorizedUser);
        given(authorizedUser.required()).willReturn(false);

        // when
        SiteUser resolveSiteUser = (SiteUser) authorizedUserResolver.resolveArgument(parameter, null, null, null);

        // then
        assertThat(resolveSiteUser).isEqualTo(siteUser);
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

    private SiteUser createAndSaveSiteUser() {
        SiteUser siteUser = new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
        return siteUserRepository.save(siteUser);
    }

    private SiteUserAuthentication createAuthenticationWithUser(SiteUser siteUser) {
        SiteUserDetails userDetails = new SiteUserDetails(siteUser);
        return new SiteUserAuthentication("token", userDetails);
    }
}
