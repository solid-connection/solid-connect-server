package com.example.solidconnection.security.filter;

import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Date;
import java.util.Objects;

import static com.example.solidconnection.auth.domain.TokenType.BLACKLIST;
import static com.example.solidconnection.common.exception.ErrorCode.USER_ALREADY_SIGN_OUT;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.spy;

@TestContainerSpringBootTest
@DisplayName("로그아웃 체크 필터 테스트")
class SignOutCheckFilterTest {

    @Autowired
    private SignOutCheckFilter signOutCheckFilter;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtProperties jwtProperties;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    private final String subject = "subject";

    @BeforeEach
    void setUp() {
        response = new MockHttpServletResponse();
        filterChain = spy(FilterChain.class);
        Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection()
                .serverCommands()
                .flushDb();
    }

    @Test
    void 로그아웃한_토큰이면_예외를_응답한다() {
        // given
        String token = createToken(subject);
        request = createRequest(token);
        String refreshTokenKey = BLACKLIST.addPrefix(token);
        redisTemplate.opsForValue().set(refreshTokenKey, token);

        // when & then
        assertThatCode(() -> signOutCheckFilter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(CustomException.class)
                .hasMessage(USER_ALREADY_SIGN_OUT.getMessage());
        then(filterChain).shouldHaveNoMoreInteractions();
    }

    @Test
    void 토큰이_없으면_다음_필터로_전달한다() throws Exception {
        // given
        request = new MockHttpServletRequest();

        // when
        signOutCheckFilter.doFilterInternal(request, response, filterChain);

        // then
        then(filterChain).should().doFilter(request, response);
    }

    @Test
    void 로그아웃하지_않은_토큰이면_다음_필터로_전달한다() throws Exception {
        // given
        String token = createToken(subject);
        request = createRequest(token);

        // when
        signOutCheckFilter.doFilterInternal(request, response, filterChain);

        // then
        then(filterChain).should().doFilter(request, response);
    }

    private String createToken(String subject) {
            return Jwts.builder()
                    .setSubject(subject)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 1000))
                    .signWith(SignatureAlgorithm.HS256, jwtProperties.secret())
                    .compact();
    }

    private HttpServletRequest createRequest(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
