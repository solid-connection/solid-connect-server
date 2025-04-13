package com.example.solidconnection.e2e;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;

public class DynamicFixture { // todo: test fixture 개선 작업 이후, 이 클래스의 사용이 대체되면 삭제 필요

    public static SiteUser createSiteUserByEmail(String email) {
        return new SiteUser(
                email,
                "nickname",
                "profileImage",
                PreparationStatus.CONSIDERING,
                Role.MENTEE
        );
    }
}
