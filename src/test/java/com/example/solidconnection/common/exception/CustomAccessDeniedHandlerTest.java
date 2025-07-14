package com.example.solidconnection.common.exception;

import com.example.solidconnection.common.response.ErrorResponse;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;

import static com.example.solidconnection.common.exception.ErrorCode.ACCESS_DENIED;
import static org.assertj.core.api.Assertions.assertThat;

@TestContainerSpringBootTest
@DisplayName("커스텀 인가 예외 처리 테스트")
class CustomAccessDeniedHandlerTest {

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

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
    void 권한이_없는_사용자_접근시_403_예외가_발생한다() throws IOException {
        // given
        AccessDeniedException accessDeniedException = new AccessDeniedException(ACCESS_DENIED.getMessage());

        // when
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // then
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(response.getStatus()).isEqualTo(ACCESS_DENIED.getCode());
        assertThat(errorResponse.message()).isEqualTo(ACCESS_DENIED.getMessage());
    }
}
