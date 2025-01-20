package com.example.solidconnection.auth.service;

import com.example.solidconnection.auth.client.KakaoOAuthClient;
import com.example.solidconnection.auth.dto.SignInResponse;
import com.example.solidconnection.auth.dto.kakao.FirstAccessResponse;
import com.example.solidconnection.auth.dto.kakao.KakaoCodeRequest;
import com.example.solidconnection.auth.dto.kakao.KakaoOauthResponse;
import com.example.solidconnection.auth.dto.kakao.KakaoUserInfoDto;
import com.example.solidconnection.auth.domain.TokenType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SignInService {

    private final TokenProvider tokenProvider;
    private final SiteUserRepository siteUserRepository;
    private final KakaoOAuthClient kakaoOAuthClient;

    /*
     * 카카오에서 받아온 사용자 정보에 있는 이메일을 통해 기존 회원인지, 신규 회원인지 판별하고, 이에 따라 다르게 응답한다.
     * 기존 회원 : 로그인
     * - 우리 서비스의 탈퇴 회원 방침을 적용한다. (계정 복구 기간 안에 접속하면 탈퇴를 무효화)
     * - 액세스 토큰과 리프레시 토큰을 발급한다.
     * 신규 회원 : 회원가입 페이지로 리다이렉트할 때 필요한 정보 제공
     * - 회원가입 시 입력하는 '닉네임'과 '프로필 사진' 부분을 미리 채우기 위해 사용자 정보를 리턴한다.
     * - 또한, 우리 서비스에서 카카오 인증을 받았는지 나타내기 위한 'kakaoOauthToken' 을 발급해서 응답한다.
     * - 회원가입할 때 클라이언트는 이때 발급받은 kakaoOauthToken 를 요청에 포함해 요청한다. (SignUpService 참고)
     * */
    @Transactional
    public KakaoOauthResponse signIn(KakaoCodeRequest kakaoCodeRequest) {
        KakaoUserInfoDto kakaoUserInfoDto = kakaoOAuthClient.processOauth(kakaoCodeRequest.code());
        String email = kakaoUserInfoDto.kakaoAccountDto().email();
        boolean isAlreadyRegistered = siteUserRepository.existsByEmail(email);

        if (isAlreadyRegistered) {
            resetQuitedAt(email);
            return getSignInInfo(email);
        }

        return getFirstAccessInfo(kakaoUserInfoDto);
    }

    // 계적 복구 기한이 지난 회원은 자정마다 삭제된다. (UserRemovalScheduler 참고)
    // 따라서 DB 에서 조회되었다면 아직 기한이 지나지 않았다는 뜻이므로, 탈퇴 날짜를 초기화한다.
    private void resetQuitedAt(String email) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        if (siteUser.getQuitedAt() == null) {
            return;
        }

        siteUser.setQuitedAt(null);
    }

    private SignInResponse getSignInInfo(String email) {
        String accessToken = tokenProvider.generateToken(email, TokenType.ACCESS);
        String refreshToken = tokenProvider.generateToken(email, TokenType.REFRESH);
        tokenProvider.saveToken(refreshToken, TokenType.REFRESH);
        return new SignInResponse(true, accessToken, refreshToken);
    }

    private FirstAccessResponse getFirstAccessInfo(KakaoUserInfoDto kakaoUserInfoDto) {
        String kakaoOauthToken = tokenProvider.generateToken(kakaoUserInfoDto.kakaoAccountDto().email(), TokenType.KAKAO_OAUTH);
        tokenProvider.saveToken(kakaoOauthToken, TokenType.KAKAO_OAUTH);
        return FirstAccessResponse.of(kakaoUserInfoDto, kakaoOauthToken);
    }
}
