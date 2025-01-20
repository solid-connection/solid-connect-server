package com.example.solidconnection.siteuser.service;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.MyPageResponse;
import com.example.solidconnection.siteuser.dto.MyPageUpdateResponse;
import com.example.solidconnection.siteuser.repository.LikedUniversityRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import com.example.solidconnection.university.domain.LikedUniversity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.example.solidconnection.support.integration.TestDataSetUpHelper.괌대학_A_지원_정보;
import static com.example.solidconnection.support.integration.TestDataSetUpHelper.메이지대학_지원_정보;
import static com.example.solidconnection.support.integration.TestDataSetUpHelper.코펜하겐IT대학_지원_정보;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("유저 서비스 테스트")
class SiteUserServiceTest extends BaseIntegrationTest {

    @Autowired
    private SiteUserService siteUserService;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private LikedUniversityRepository likedUniversityRepository;

    @Test
    void 마이페이지_정보를_조회한다() {
        // given
        SiteUser testUser = createSiteUser();
        int likedUniversityCount = createLikedUniversities(testUser);

        // when
        MyPageResponse response = siteUserService.getMyPageInfo(testUser.getEmail());

        // then
        Assertions.assertAll(
                () -> assertThat(response.nickname()).isEqualTo(testUser.getNickname()),
                () -> assertThat(response.profileImageUrl()).isEqualTo(testUser.getProfileImageUrl()),
                () -> assertThat(response.role()).isEqualTo(testUser.getRole()),
                () -> assertThat(response.birth()).isEqualTo(testUser.getBirth()),
                () -> assertThat(response.email()).isEqualTo(testUser.getEmail()),
                () -> assertThat(response.likedPostCount()).isEqualTo(testUser.getPostLikeList().size()),
                () -> assertThat(response.likedUniversityCount()).isEqualTo(likedUniversityCount)
        );
    }

    @Test
    void 내_정보를_수정하기_위한_마이페이지_정보를_조회한다() {
        // given
        SiteUser testUser = createSiteUser();

        // when
        MyPageUpdateResponse response = siteUserService.getMyPageInfoToUpdate(testUser.getEmail());

        // then
        Assertions.assertAll(
                () -> assertThat(response.nickname()).isEqualTo(testUser.getNickname()),
                () -> assertThat(response.profileImageUrl()).isEqualTo(testUser.getProfileImageUrl())
        );
    }

    private SiteUser createSiteUser() {
        SiteUser siteUser = new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
        return siteUserRepository.save(siteUser);
    }

    private int createLikedUniversities(SiteUser testUser) {
        LikedUniversity likedUniversity1 = new LikedUniversity(null, 괌대학_A_지원_정보, testUser);
        LikedUniversity likedUniversity2 = new LikedUniversity(null, 메이지대학_지원_정보, testUser);
        LikedUniversity likedUniversity3 = new LikedUniversity(null, 코펜하겐IT대학_지원_정보, testUser);

        likedUniversityRepository.save(likedUniversity1);
        likedUniversityRepository.save(likedUniversity2);
        likedUniversityRepository.save(likedUniversity3);
        return likedUniversityRepository.countBySiteUser_Email(testUser.getEmail());
    }
}
