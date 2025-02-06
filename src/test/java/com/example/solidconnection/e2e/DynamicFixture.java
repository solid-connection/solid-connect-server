package com.example.solidconnection.e2e;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.auth.dto.oauth.KakaoUserInfoDto;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.LanguageTestType;
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

    public static SiteUser createSiteUserByNickName(String nickname) {
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
                "koreanName",
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

    public static Gpa createDummyGpa() {
        return new Gpa(3.5, 4.0, "gpaReportUrl");
    }

    public static LanguageTest createDummyLanguageTest() {
        return new LanguageTest(LanguageTestType.TOEIC, "900", "toeicReportUrl");
    }
}
