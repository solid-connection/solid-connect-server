package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.dto.SignInResponseDto;
import com.example.solidconnection.auth.dto.kakao.FirstAccessResponseDto;
import com.example.solidconnection.auth.dto.kakao.KakaoCodeDto;
import com.example.solidconnection.auth.dto.kakao.KakaoOauthResponseDto;
import com.example.solidconnection.auth.dto.kakao.KakaoUserInfoDto;
import com.example.solidconnection.config.token.TokenService;
import com.example.solidconnection.config.token.TokenType;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SignInService {

    private final TokenService tokenService;
    private final SiteUserRepository siteUserRepository;
    private final KakaoOAuthService kakaoOAuthService;

    /*
    * 카카오에서 받아온 사용자 정보에 있는 이메일을 통해 기존 회원인지, 신규 회원인지 판별하고, 이에 따라 다르게 응답한다.
    * 기존 회원 : 로그인
    * - 우리 서비스의 탈퇴 회원 방침을 적용한다. (탈퇴했더라도 정해진 기간 안에 접속하면 탈퇴를 무효화)
    * - 액세스 토큰과 리프레시 토큰을 발급한다.
    * 신규 회원 : 회원가입 페이지로 리다이렉트할 때 필요한 정보 제공
    * - 회원가입 시 입력하는 '닉네임'과 '프로필 사진' 부분을 미리 채우기 위해 사용자 정보를 리턴한다.
    * - 또한, 우리 서비스에서 카카오 인증을 받았는지 나타내기 위한 'kakaoOauthToken' 을 발급해서 응답한다.
    * - 회원가입할 때 클라이언트는 이때 발급받은 kakaoOauthToken 를 요청에 포함해 요청한다.
    * */
    @Transactional(readOnly = true)
    public KakaoOauthResponseDto signIn(KakaoCodeDto kakaoCodeDto) {
        KakaoUserInfoDto kakaoUserInfoDto = kakaoOAuthService.processOauth(kakaoCodeDto.getCode());
        String email = kakaoUserInfoDto.getKakaoAccount().getEmail();
        boolean isAlreadyRegistered = siteUserRepository.existsByEmail(email);

        if (isAlreadyRegistered) {
            resetQuitedAt(email);
            return getSignInInfo(email);
        }

        return getFirstAccessInfo(kakaoUserInfoDto);
    }

    private void resetQuitedAt(String email) {
        siteUserRepository
                .getByEmail(email)
                .setQuitedAt(null);
    }

    private SignInResponseDto getSignInInfo(String email) {
        String accessToken = tokenService.generateToken(email, TokenType.ACCESS);
        String refreshToken = tokenService.generateToken(email, TokenType.REFRESH);
        tokenService.saveToken(refreshToken, TokenType.REFRESH);
        return SignInResponseDto.builder()
                .registered(true)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private FirstAccessResponseDto getFirstAccessInfo(KakaoUserInfoDto kakaoUserInfoDto) {
        String kakaoOauthToken = tokenService.generateToken(
                kakaoUserInfoDto.getKakaoAccount().getEmail(), TokenType.KAKAO_OAUTH);
        return FirstAccessResponseDto.of(kakaoUserInfoDto, kakaoOauthToken);
    }
}
