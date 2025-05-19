package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.dto.ReissueRequest;
import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.example.solidconnection.common.exception.ErrorCode.REFRESH_TOKEN_EXPIRED;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final AuthTokenProvider authTokenProvider;

    /*
     * 로그아웃한다.
     * - 엑세스 토큰을 블랙리스트에 추가한다.
     * - 리프레시 토큰을 삭제한다.
     * */
    public void signOut(String token) {
        AccessToken accessToken = authTokenProvider.toAccessToken(token);
        authTokenProvider.deleteRefreshTokenByAccessToken(accessToken);
        authTokenProvider.addToBlacklist(accessToken);
    }

    /*
     * 탈퇴한다.
     * - 탈퇴한 시점의 다음날을 탈퇴일로 잡는다.
     * - e.g. 2024-01-01 18:00 탈퇴 시, 2024-01-02 00:00 가 탈퇴일이 된다.
     * - 로그아웃한다.
     * */
    @Transactional
    public void quit(SiteUser siteUser, String token) { // todo: #299를 작업하며 인자를 (String token)만 받도록 수정해야 함
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        siteUser.setQuitedAt(tomorrow);
        signOut(token);
    }

    /*
     * 액세스 토큰을 재발급한다.
     * - 유효한 리프레시토큰이면, 액세스 토큰을 재발급한다.
     * - 그렇지 않으면 예외를 발생시킨다.
     * */
    public ReissueResponse reissue(ReissueRequest reissueRequest) {
        // 리프레시 토큰 확인
        String requestedRefreshToken = reissueRequest.refreshToken();
        if (!authTokenProvider.isValidRefreshToken(requestedRefreshToken)) {
            throw new CustomException(REFRESH_TOKEN_EXPIRED);
        }
        // 액세스 토큰 재발급
        Subject subject = authTokenProvider.parseSubject(requestedRefreshToken);
        AccessToken newAccessToken = authTokenProvider.generateAccessToken(subject);
        return ReissueResponse.from(newAccessToken);
    }
}
