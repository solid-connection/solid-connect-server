package com.example.solidconnection.config.security;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.example.solidconnection.custom.exception.ErrorCode.AUTHENTICATION_FAILED;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(AUTHENTICATION_FAILED, authException.getMessage());
        writeResponse(response, errorResponse);
    }

    public void customCommence(HttpServletResponse response, CustomException customException) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(customException);
        writeResponse(response, errorResponse);
    }

    private void writeResponse(HttpServletResponse response, ErrorResponse errorResponse) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
