package com.example.solidconnection.config.security;

import com.example.solidconnection.support.TestContainerSpringBootTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.spy;

@TestContainerSpringBootTest
@DisplayName("토큰 인증 필터 테스트")
class JwtAuthenticationFilterTest {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtProperties jwtProperties;

    @MockBean
    private SiteUserDetailsService siteUserDetailsService;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach()
    void setUp() {
        response = new MockHttpServletResponse();
        filterChain = spy(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    public void 토큰이_없으면_다음_필터로_진행한다() throws Exception {
        // given
        request = new MockHttpServletRequest();

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        then(filterChain).should().doFilter(request, response);
    }

    @Nested
    class 토큰이_있으면_컨텍스트에_저장한다 {

        @Test
        void 유효한_토큰을_컨텍스트에_저장한다() throws Exception {
            // given
            Date validExpiration = new Date(System.currentTimeMillis() + 1000);
            String token = createTokenWithExpiration(validExpiration);
            request = createRequestWithToken(token);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            assertThat(SecurityContextHolder.getContext().getAuthentication())
                    .isExactlyInstanceOf(SiteUserAuthentication.class);
            then(filterChain).should().doFilter(request, response);
        }

        @Test
        void 만료된_토큰을_컨텍스트에_저장한다() throws Exception {
            // given
            Date invalidExpiration = new Date(System.currentTimeMillis() - 1000);
            String token = createTokenWithExpiration(invalidExpiration);
            request = createRequestWithToken(token);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            assertThat(SecurityContextHolder.getContext().getAuthentication())
                    .isExactlyInstanceOf(ExpiredTokenAuthentication.class);
            then(filterChain).should().doFilter(request, response);
        }
    }

    private String createTokenWithExpiration(Date expiration) {
        return Jwts.builder()
                .setSubject("1")
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secret())
                .compact();
    }

    private HttpServletRequest createRequestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
