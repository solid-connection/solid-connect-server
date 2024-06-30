package com.example.solidconnection.auth.service;


import com.example.solidconnection.auth.dto.ReissueResponse;
import com.example.solidconnection.config.token.TokenService;
import com.example.solidconnection.config.token.TokenType;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static com.example.solidconnection.custom.exception.ErrorCode.REFRESH_TOKEN_EXPIRED;

@RequiredArgsConstructor
@Transactional
@Service
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final TokenService tokenService;
    private final SiteUserRepository siteUserRepository;

    public boolean signOut(String email){
        redisTemplate.opsForValue().set(
                TokenType.REFRESH.getPrefix() + email,
                "signOut",
                TokenType.REFRESH.getExpireTime(),
                TimeUnit.MILLISECONDS
        );
        return true;
    }

    public boolean quit(String email){
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        siteUser.setQuitedAt(LocalDate.now().plusDays(1));
        return true;
    }

    public ReissueResponse reissue(String email) {
        // 리프레시 토큰 만료 확인
        String refreshTokenKey= TokenType.REFRESH.getPrefix() + email;
        String refreshToken = redisTemplate.opsForValue().get(refreshTokenKey);
        if (ObjectUtils.isEmpty(refreshToken)) {
            throw new CustomException(REFRESH_TOKEN_EXPIRED);
        }
        // 엑세스 토큰 재발급
        String newAccessToken = tokenService.generateToken(email, TokenType.ACCESS);
        return new ReissueResponse(newAccessToken);
    }
}
