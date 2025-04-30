package com.example.solidconnection.auth.service;


import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.config.security.JwtProperties;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static com.example.solidconnection.custom.exception.ErrorCode.REFRESH_TOKEN_EXPIRED;
import static com.example.solidconnection.util.JwtUtils.parseSubject;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final AuthTokenProvider authTokenProvider;
    private final JwtProperties jwtProperties;

    /*
     * 로그아웃 한다.
     * - 엑세스 토큰을 블랙리스트에 추가한다.
     * */
    public void signOut(String accessToken) {
        authTokenProvider.generateAndSaveBlackListToken(accessToken);
    }

    /*
     * 탈퇴한다.
     * - 탈퇴한 시점의 다음날을 탈퇴일로 잡는다.
     * - e.g. 2024-01-01 18:00 탈퇴 시, 2024-01-02 00:00 가 탈퇴일이 된다.
     * */
    @Transactional
    public void quit(SiteUser siteUser) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        siteUser.setQuitedAt(tomorrow);
    }

    /*
     * 액세스 토큰을 재발급한다.
     * - 리프레시 토큰이 만료되었거나, 존재하지 않는다면 예외 응답을 반환한다.
     * - 리프레시 토큰이 존재한다면, 액세스 토큰을 재발급한다.
     * */
    public ReissueResponse reissue(String accessToken) {
        // 리프레시 토큰 만료 확인
        String subject = parseSubject(accessToken, jwtProperties.secret());
        Optional<String> optionalRefreshToken = authTokenProvider.findRefreshToken(subject);
        if (optionalRefreshToken.isEmpty()) {
            throw new CustomException(REFRESH_TOKEN_EXPIRED);
        }
        // 액세스 토큰 재발급
        String newAccessToken = authTokenProvider.generateAccessToken(accessToken);
        return new ReissueResponse(newAccessToken);
    }
}
