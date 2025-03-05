package com.example.solidconnection.siteuser.service;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.NicknameExistsResponse;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("유저 서비스 테스트")
class SiteUserServiceTest extends BaseIntegrationTest {

    @Autowired
    private SiteUserService siteUserService;

    @Autowired
    private SiteUserRepository siteUserRepository;

    private SiteUser siteUser;

    @BeforeEach
    void setUp() {
        siteUser = createSiteUser();
    }

    @Nested
    class 닉네임_중복_검사 {

        @Test
        void 존재하는_닉네임이면_true를_반환한다() {
            // when
            NicknameExistsResponse response = siteUserService.checkNicknameExists(siteUser.getNickname());

            // then
            assertThat(response.exists()).isTrue();
        }

        @Test
        void 존재하지_않는_닉네임이면_false를_반환한다() {
            // when
            NicknameExistsResponse response = siteUserService.checkNicknameExists("nonExistingNickname");

            // then
            assertThat(response.exists()).isFalse();
        }
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
}
