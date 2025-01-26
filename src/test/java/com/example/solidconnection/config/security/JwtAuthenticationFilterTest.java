package com.example.solidconnection.config.security;

import com.example.solidconnection.custom.exception.CustomException;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.spy;

@TestContainerSpringBootTest
@DisplayName("토큰 인증 필터 테스트")
class JwtAuthenticationFilterTest {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtProperties jwtProperties;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        response = new MockHttpServletResponse();
        filterChain = spy(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    public void 유효한_토큰에_대한_인증_정보를_저장한다() throws Exception {
        // given
        String token = Jwts.builder()
                .setSubject("subject")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secret())
                .compact();
        request = createRequestWithToken(token);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isExactlyInstanceOf(JwtAuthentication.class);
        then(filterChain).should().doFilter(request, response);
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
    class 유효하지_않은_토큰으로_인증하면_예외를_응답한다 {

        @Test
        public void 만료된_토큰으로_인증하면_예외를_응답한다() throws Exception {
            // given
            String token = Jwts.builder()
                    .setSubject("subject")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() - 1000))
                    .signWith(SignatureAlgorithm.HS256, jwtProperties.secret())
                    .compact();
            request = createRequestWithToken(token);

            // when & then
            assertThatCode(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_TOKEN.getMessage());
            then(filterChain).shouldHaveNoMoreInteractions();
        }

        @Test
        public void 서명하지_않은_토큰으로_인증하면_예외를_응답한다() throws Exception {
            // given
            String token = Jwts.builder()
                    .setSubject("subject")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() - 1000))
                    .signWith(SignatureAlgorithm.HS256, "wrongSecretKey")
                    .compact();
            request = createRequestWithToken(token);

            // when & then
            assertThatCode(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_TOKEN.getMessage());
            then(filterChain).shouldHaveNoMoreInteractions();
        }
    }

    private HttpServletRequest createRequestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
