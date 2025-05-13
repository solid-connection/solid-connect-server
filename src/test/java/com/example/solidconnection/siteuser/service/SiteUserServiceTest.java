package com.example.solidconnection.siteuser.service;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.NicknameExistsResponse;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@TestContainerSpringBootTest
@DisplayName("유저 서비스 테스트")
class SiteUserServiceTest {

    @Autowired
    private SiteUserService siteUserService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    private SiteUser 테스트_유저;

    @BeforeEach
    void setUp() {
        테스트_유저 = siteUserFixture.테스트_유저();
    }

    @Nested
    class 닉네임_중복_검사 {

        @Test
        void 존재하는_닉네임이면_true를_반환한다() {
            // when
            NicknameExistsResponse response = siteUserService.checkNicknameExists(테스트_유저.getNickname());

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
}
