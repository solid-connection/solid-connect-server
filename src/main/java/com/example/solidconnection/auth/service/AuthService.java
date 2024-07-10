package com.example.solidconnection.auth.service;


import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.config.token.TokenService;
import com.example.solidconnection.config.token.TokenType;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static com.example.solidconnection.config.token.TokenValidator.SIGN_OUT_VALUE;
import static com.example.solidconnection.custom.exception.ErrorCode.REFRESH_TOKEN_EXPIRED;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final TokenService tokenService;
    private final SiteUserRepository siteUserRepository;

    /*
     * 로그아웃 한다.
     * - 리프레시 토큰을 무효화하기 위해 리프레시 토큰의 value 를 변경한다.
     * - 어떤 사용자가 엑세스 토큰으로 인증이 필요한 기능을 사용하려 할 때, 로그아웃 검증이 진행되는데,
     * - 이때 리프레시 토큰의 value 가 SIGN_OUT_VALUE 이면 예외 응답이 반환된다.
     *   - (TokenValidator.validateNotSignOut() 참고)
     * */
    public void signOut(String email) {
        redisTemplate.opsForValue().set(
                TokenType.REFRESH.addTokenPrefixToSubject(email),
                SIGN_OUT_VALUE,
                TokenType.REFRESH.getExpireTime(),
                TimeUnit.MILLISECONDS
        );
    }

    /*
     * 탈퇴한다.
     * - 탈퇴한 시점의 다음날을 탈퇴일로 잡는다.
     * - e.g. 2024-01-01 18:00 탈퇴 시, 2024-01-02 00:00 가 탈퇴일이 된다.
     * */
    @Transactional
    public void quit(String email) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        siteUser.setQuitedAt(tomorrow);
    }

    /*
     * 액세스 토큰을 재발급한다.
     * - 리프레시 토큰이 만료되었거나, 존재하지 않는다면 예외 응답을 반환한다.
     * - 리프레시 토큰이 존재한다면, 액세스 토큰을 재발급한다.
     * */
    public ReissueResponse reissue(String email) {
        // 리프레시 토큰 만료 확인
        String refreshTokenKey = TokenType.REFRESH.addTokenPrefixToSubject(email);
        String refreshToken = redisTemplate.opsForValue().get(refreshTokenKey);
        if (ObjectUtils.isEmpty(refreshToken)) {
            throw new CustomException(REFRESH_TOKEN_EXPIRED);
        }
        // 액세스 토큰 재발급
        String newAccessToken = tokenService.generateToken(email, TokenType.ACCESS);
        tokenService.saveToken(newAccessToken, TokenType.ACCESS);
        return new ReissueResponse(newAccessToken);
    }
}
