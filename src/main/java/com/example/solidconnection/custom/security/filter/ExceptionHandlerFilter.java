package com.example.solidconnection.custom.security.filter;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
        } catch (Exception e) {
            generalCommence(response, e);
        }
    }

    public void customCommence(HttpServletResponse response, CustomException customException) throws IOException {
        SecurityContextHolder.clearContext();
        ErrorResponse errorResponse = new ErrorResponse(customException);
        writeResponse(response, errorResponse);
    }

    public void generalCommence(HttpServletResponse response, Exception exception) throws IOException {
        SecurityContextHolder.clearContext();
        ErrorResponse errorResponse = new ErrorResponse(AUTHENTICATION_FAILED, exception.getMessage());
        writeResponse(response, errorResponse);
    }

    private void writeResponse(HttpServletResponse response, ErrorResponse errorResponse) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
