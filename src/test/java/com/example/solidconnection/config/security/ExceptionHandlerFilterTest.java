package com.example.solidconnection.config.security;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@TestContainerSpringBootTest
class ExceptionHandlerFilterTest {

    @Autowired
    private ExceptionHandlerFilter exceptionHandlerFilter;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach()
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = spy(FilterChain.class);
    }

    @Test
    void 필터_체인에서_예외가_발생하면_SecurityContext_를_초기화한다() throws Exception {
        // given
        Authentication authentication = mock(TestingAuthenticationToken.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        willThrow(new RuntimeException()).given(filterChain).doFilter(request, response);

        // when
        exceptionHandlerFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void 필터_체인에서_예외가_발생하지_않으면_다음_필터로_진행한다() throws Exception {
        // given
        willDoNothing().given(filterChain).doFilter(request, response);

        // when
        exceptionHandlerFilter.doFilterInternal(request, response, filterChain);

        // then
        then(filterChain).should().doFilter(request, response);
    }

    @ParameterizedTest
    @MethodSource("provideException")
    void 필터_체인에서_예외가_발생하면_예외_응답을_반환한다(Throwable throwable) throws Exception {
        // given
        willThrow(throwable).given(filterChain).doFilter(request, response);

        // when
        exceptionHandlerFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private static Stream<Throwable> provideException() {
        return Stream.of(
                new RuntimeException(),
                new CustomException(ErrorCode.INVALID_TOKEN)
        );
    }
}
