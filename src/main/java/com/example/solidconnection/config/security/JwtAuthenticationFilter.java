package com.example.solidconnection.config.security;

import com.example.solidconnection.config.token.TokenProvider;
import com.example.solidconnection.config.token.TokenValidator;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.JwtExpiredTokenException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;

import static com.example.solidconnection.custom.exception.ErrorCode.ACCESS_TOKEN_EXPIRED;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;
    private final TokenValidator tokenValidator;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 인증 정보를 저장할 필요 없는 url
        AntPathMatcher pathMatcher = new AntPathMatcher();
        for (String endpoint : getPermitAllEndpoints()) {
            if (pathMatcher.match(endpoint, request.getRequestURI())) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 토큰 검증
        try {
            String token = this.resolveAccessTokenFromRequest(request); // 웹 요청에서 토큰 추출
            if (token != null) { // 토큰이 있어야 검증 - 토큰 유무에 대한 다른 처리를 컨트롤러에서 할 수 있음
                try {
                    String requestURI = request.getRequestURI();
                    if (requestURI.equals("/auth/reissue")) {
                        Authentication auth = this.tokenProvider.getAuthentication(token);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        filterChain.doFilter(request, response);
                        return;
                    }
                    tokenValidator.validateAccessToken(token); // 액세스 토큰 검증 - 비어있는지, 유효한지, 리프레시 토큰, 로그아웃
                } catch (ExpiredJwtException e) {
                    throw new JwtExpiredTokenException(ACCESS_TOKEN_EXPIRED.getMessage());
                }
                Authentication auth = this.tokenProvider.getAuthentication(token); // 토큰에서 인증 정보 가져옴
                SecurityContextHolder.getContext().setAuthentication(auth);// 인증 정보를 보안 컨텍스트에 설정
            }
        } catch (JwtExpiredTokenException e) {
            SecurityContextHolder.clearContext();
            jwtAuthenticationEntryPoint.commence(request, response, e);
            return;
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            jwtAuthenticationEntryPoint.commence(request, response, e);
            return;
        } catch (CustomException e) {
            jwtAuthenticationEntryPoint.customCommence(response, e);
            return;
        }
        filterChain.doFilter(request, response); // 다음 필터로 요청과 응답 전달
    }

    private String resolveAccessTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);

        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) { // 토큰이 비어 있지 않고, Bearer로 시작한다면
            return token.substring(TOKEN_PREFIX.length()); // Bearer 제외한 실제 토큰 부분 반환
        }
        return null;
    }

    private HashSet<String> getPermitAllEndpoints() {
        var permitAllEndpoints = new HashSet<String>();

        // 서버 정상 작동 확인
        permitAllEndpoints.add("/");
        permitAllEndpoints.add("/index.html");
        permitAllEndpoints.add("/favicon.ico");

        // 이미지 업로드
        permitAllEndpoints.add("/file/profile/pre");

        // 토큰이 필요하지 않은 인증
        permitAllEndpoints.add("/auth/kakao");
        permitAllEndpoints.add("/auth/sign-up");

        // 대학교 정보
        permitAllEndpoints.add("/university/search/**");

        return permitAllEndpoints;
    }
}
