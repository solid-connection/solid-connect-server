package com.example.solidconnection.custom.security.filter;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.custom.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.example.solidconnection.custom.exception.ErrorCode.ACCESS_DENIED;
import static com.example.solidconnection.custom.exception.ErrorCode.AUTHENTICATION_FAILED;

@Component
@RequiredArgsConstructor
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (CustomException e) {
            customCommence(response, e);
        } catch (AccessDeniedException e) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof AnonymousAuthenticationToken) {
                generalCommence(response, e, AUTHENTICATION_FAILED);
            } else {
                generalCommence(response, e, ACCESS_DENIED);
            }
        } catch (Exception e) {
            generalCommence(response, e, AUTHENTICATION_FAILED);
        }
    }

    public void customCommence(HttpServletResponse response, CustomException customException) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(customException);
        writeResponse(response, errorResponse, customException.getCode());
    }

    public void generalCommence(HttpServletResponse response, Exception exception, ErrorCode errorCode) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(errorCode, exception.getMessage());
        writeResponse(response, errorResponse, errorCode.getCode());
    }

    private void writeResponse(HttpServletResponse response, ErrorResponse errorResponse, int statusCode) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
