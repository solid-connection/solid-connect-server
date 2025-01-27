package com.example.solidconnection.custom.resolver;

import com.example.solidconnection.custom.security.authentication.ExpiredTokenAuthentication;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

@TestContainerSpringBootTest
@DisplayName("만료된 토큰 argument resolver 테스트")
class ExpiredTokenResolverTest {

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Autowired
    private ExpiredTokenResolver expiredTokenResolver;

    @Test
    void security_context_에_저장된_만료시간을_검증하지_않는_토큰을_반환한다() throws Exception {
        // given
        ExpiredTokenAuthentication authentication = new ExpiredTokenAuthentication("token");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        ExpiredTokenAuthentication expiredTokenAuthentication = (ExpiredTokenAuthentication) expiredTokenResolver.resolveArgument(null, null, null, null);

        // then
        assertThat(expiredTokenAuthentication.getToken()).isEqualTo("token");
    }

    @Test
    void security_context_에_저장된_만료시간을_검증하지_않는_토큰이_없으면_null_을_반환한다() throws Exception {
        // when, then
        assertThat(expiredTokenResolver.resolveArgument(null, null, null, null)).isNull();
    }
}
