package com.example.solidconnection.auth.service;

import static com.example.solidconnection.common.exception.ErrorCode.REFRESH_TOKEN_EXPIRED;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.auth.token.TokenBlackListService;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final AuthTokenProvider authTokenProvider;
    private final TokenBlackListService tokenBlackListService;
    private final SiteUserRepository siteUserRepository;

    /*
     * 로그아웃한다.
     * - 엑세스 토큰을 블랙리스트에 추가한다.
     * - 리프레시 토큰을 삭제한다.
     * */
    public void signOut(String token) {
        Subject subject = authTokenProvider.parseSubject(token);
        long siteUserId = Long.parseLong(subject.value());
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        AccessToken accessToken = authTokenProvider.generateAccessToken(subject, siteUser.getRole());
        authTokenProvider.deleteRefreshTokenByAccessToken(accessToken);
        tokenBlackListService.addToBlacklist(accessToken);
    }

    /*
     * 탈퇴한다.
     * - 탈퇴한 시점의 다음날을 탈퇴일로 잡는다.
     * - e.g. 2024-01-01 18:00 탈퇴 시, 2024-01-02 00:00 가 탈퇴일이 된다.
     * - 로그아웃한다.
     * */
    @Transactional
    public void quit(long siteUserId, String token) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        siteUser.setQuitedAt(tomorrow);
        signOut(token);
    }

    /*
     * 액세스 토큰을 재발급한다.
     * - 유효한 리프레시토큰이면, 액세스 토큰을 재발급한다.
     * - 그렇지 않으면 예외를 발생시킨다.
     * */
    public ReissueResponse reissue(String requestedRefreshToken) {
        // 리프레시 토큰 확인
        if (!authTokenProvider.isValidRefreshToken(requestedRefreshToken)) {
            throw new CustomException(REFRESH_TOKEN_EXPIRED);
        }
        Subject subject = authTokenProvider.parseSubject(requestedRefreshToken);
        long siteUserId = Long.parseLong(subject.value());

        // 액세스 토큰 재발급
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        AccessToken newAccessToken = authTokenProvider.generateAccessToken(subject, siteUser.getRole());
        return ReissueResponse.from(newAccessToken);
    }
}
