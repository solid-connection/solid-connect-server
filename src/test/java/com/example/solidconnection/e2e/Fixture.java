package com.example.solidconnection.e2e;

import com.example.solidconnection.auth.dto.kakao.KakaoUserInfoDto;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;

public class Fixture {

    public static SiteUser createSiteUserFixtureByEmail(String email) {
        return new SiteUser(
                email,
                "nickname",
                "profileImage",
                "2000-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.FEMALE
        );
    }

    public static SiteUser createSiteUserFixtureByNickName(String nickname) {
        return new SiteUser(
                "email@email.com",
                nickname,
                "profileImage",
                "2000-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.FEMALE
        );
    }

    public static KakaoUserInfoDto createKakaoUserInfoDtoByEmail(String email) {
        return new KakaoUserInfoDto(
                new KakaoUserInfoDto.KakaoAccountDto(
                        new KakaoUserInfoDto.KakaoAccountDto.KakaoProfileDto(
                                "nickname",
                                "profileImageUrl"
                        ),
                        email
                )
        );
    }
}
