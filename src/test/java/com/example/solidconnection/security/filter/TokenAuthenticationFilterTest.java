package com.example.solidconnection.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.spy;

import com.example.solidconnection.auth.token.config.JwtProperties;
import com.example.solidconnection.security.authentication.TokenAuthentication;
import com.example.solidconnection.security.userdetails.SiteUserDetails;
import com.example.solidconnection.security.userdetails.SiteUserDetailsService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@TestContainerSpringBootTest
@DisplayName("토큰 인증 필터 테스트")
class TokenAuthenticationFilterTest {

    @Autowired
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @MockBean // 이 테스트코드에서 사용자를 조회할 필요는 없으므로 MockBean 으로 대체
    private SiteUserDetailsService siteUserDetailsService;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach()
    void setUp() {
        response = new MockHttpServletResponse();
        filterChain = spy(FilterChain.class);
        SecurityContextHolder.clearContext();

        SiteUser siteUser = siteUserFixture.사용자(1, "test");
        SiteUserDetails userDetails = new SiteUserDetails(siteUser);
        given(siteUserDetailsService.loadUserByUsername(anyString()))
                .willReturn(userDetails);
    }

    @Test
    public void 토큰이_없으면_다음_필터로_진행한다() throws Exception {
        // given
        request = new MockHttpServletRequest();

        // when
        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        then(filterChain).should().doFilter(request, response);
    }

    @Test
    void 토큰이_있으면_컨텍스트에_저장한다() throws Exception {
        // given
        Date validExpiration = new Date(System.currentTimeMillis() + 1000);
        String token = createTokenWithExpiration(validExpiration);
        request = createRequestWithToken(token);

        // when
        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isExactlyInstanceOf(TokenAuthentication.class);
        then(filterChain).should().doFilter(request, response);
    }

    @Test
    void 토큰이_있으면_컨텍스트에_저장하고_userId를_request에_설정한다() throws Exception {
        // given
        Long expectedUserId = 1L;
        Date validExpiration = new Date(System.currentTimeMillis() + 1000);
        String token = createTokenWithExpiration(validExpiration);
        request = createRequestWithToken(token);

        // when
        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isExactlyInstanceOf(TokenAuthentication.class);
        assertThat(request.getAttribute("userId")).isEqualTo(expectedUserId);
        then(filterChain).should().doFilter(request, response);
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
