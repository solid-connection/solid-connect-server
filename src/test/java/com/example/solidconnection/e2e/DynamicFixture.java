package com.example.solidconnection.e2e;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;

public class DynamicFixture {

    public static SiteUser createSiteUserByEmail(String email) {
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
}
