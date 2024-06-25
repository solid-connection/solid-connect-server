package com.example.solidconnection.auth.dto.kakao;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirstAccessResponseDto extends KakaoOauthResponseDto {
    private boolean registered;
    private String nickname;
    private String email;
    private String profileImageUrl;
    private String kakaoOauthToken; //TODO: 변수명 변경 필요하다. 지금은 카카오 인증의 다른 요소들과 구별이 되지 않는다. '우리 서비스에서 oauth 인증을 받았음'을 더 나타냈으면 좋을 것 같다.

    public static FirstAccessResponseDto of(KakaoUserInfoDto kakaoUserInfoDto, String kakaoOauthToken){
        return FirstAccessResponseDto.builder()
                .registered(false)
                .email(kakaoUserInfoDto.getKakaoAccount().getEmail())
                .profileImageUrl(kakaoUserInfoDto.getKakaoAccount().getProfile().getProfileImageUrl())
                .nickname(kakaoUserInfoDto.getKakaoAccount().getProfile().getNickname())
                .kakaoOauthToken(kakaoOauthToken)
                .build();
    }
}
