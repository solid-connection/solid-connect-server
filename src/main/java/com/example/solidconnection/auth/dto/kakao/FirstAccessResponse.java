package com.example.solidconnection.auth.dto.kakao;

public record FirstAccessResponse(
        boolean isRegistered,
        String nickname,
        String email,
        String profileImageUrl,
        String kakaoOauthToken) implements KakaoOauthResponse { //TODO: 변수명 변경 필요하다. 지금은 카카오 인증의 다른 요소들과 구별이 되지 않는다. '우리 서비스에서 oauth 인증을 받았음'을 더 나타냈으면 좋을 것 같다.

    public static FirstAccessResponse of(KakaoUserInfoDto kakaoUserInfoDto, String kakaoOauthToken) {
        return new FirstAccessResponse(
                false,
                kakaoUserInfoDto.kakaoAccountDto().profile().nickname(),
                kakaoUserInfoDto.kakaoAccountDto().email(),
                kakaoUserInfoDto.kakaoAccountDto().profile().profileImageUrl(),
                kakaoOauthToken
        );
    }
}
