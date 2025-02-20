package com.example.solidconnection.custom.exception;

import com.example.solidconnection.custom.response.ErrorResponse;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static com.example.solidconnection.custom.exception.ErrorCode.AUTHENTICATION_FAILED;
import static org.assertj.core.api.Assertions.assertThat;

@TestContainerSpringBootTest
@DisplayName("커스텀 인증 예외 처리 테스트")
class CustomAuthenticationEntryPointTest {

    @Autowired
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void 인증되지_않은_사용자_접근시_401_예외_응답을_반환한다() throws IOException {
        // given
        AuthenticationException authException = new AuthenticationServiceException(AUTHENTICATION_FAILED.getMessage());

        // when
        authenticationEntryPoint.commence(request, response, authException);

        // then
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(response.getStatus()).isEqualTo(AUTHENTICATION_FAILED.getCode());
        assertThat(errorResponse.message()).isEqualTo(AUTHENTICATION_FAILED.getMessage());
    }
}
