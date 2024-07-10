package com.example.solidconnection.e2e;

import com.example.solidconnection.auth.dto.kakao.KakaoUserInfoDto;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import com.example.solidconnection.type.SemesterAvailableForDispatch;
import com.example.solidconnection.type.TuitionFeeType;
import com.example.solidconnection.university.domain.LanguageRequirement;
import com.example.solidconnection.university.domain.LikedUniversity;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UniversityInfoForApply;

import java.util.Set;

public class DynamicFixture {

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

    public static UniversityInfoForApply createUniversityForApply(
            String term, University university, Set<LanguageRequirement> languageRequirements) {
        return new UniversityInfoForApply(
                null,
                term,
                1,
                TuitionFeeType.HOME_UNIVERSITY_PAYMENT,
                SemesterAvailableForDispatch.ONE_SEMESTER,
                "1",
                "detailsForLanguage",
                "gpaRequirement",
                "gpaRequirementCriteria",
                "detailsForApply",
                "detailsForMajor",
                "detailsForAccommodation",
                "detailsForEnglishCourse",
                "details",
                languageRequirements,
                university);
    }

    public static LikedUniversity createLikedUniversity(
            SiteUser siteUser, UniversityInfoForApply universityInfoForApply) {
        return new LikedUniversity(null, universityInfoForApply, siteUser);
    }
}
