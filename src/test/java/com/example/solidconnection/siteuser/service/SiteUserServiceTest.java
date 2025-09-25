package com.example.solidconnection.siteuser.service;

import static com.example.solidconnection.common.exception.ErrorCode.ALREADY_BLOCKED_BY_CURRENT_USER;
import static com.example.solidconnection.common.exception.ErrorCode.CANNOT_BLOCK_YOURSELF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.NicknameExistsResponse;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.siteuser.repository.UserBlockRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("유저 서비스 테스트")
class SiteUserServiceTest {

    @Autowired
    private SiteUserService siteUserService;

    @Autowired
    private UserBlockRepository userBlockRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    private SiteUser user;

    @BeforeEach
    void setUp() {
        user = siteUserFixture.사용자();
    }

    @Nested
    class 닉네임_중복_검사 {

        @Test
        void 존재하는_닉네임이면_true를_반환한다() {
            // when
            NicknameExistsResponse response = siteUserService.checkNicknameExists(user.getNickname());

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

    @Nested
    class 유저_차단 {

        private SiteUser blockedUser;

        @BeforeEach
        void setUp() {
            blockedUser = siteUserFixture.사용자(1, "blockedUser");
        }

        @Test
        void 성공적으로_유저를_차단한다() {
            // when
            siteUserService.blockUser(user.getId(), blockedUser.getId());

            // then
            assertThat(userBlockRepository.existsByBlockerIdAndBlockedId(user.getId(), blockedUser.getId())).isTrue();
        }

        @Test
        void 자기_자신을_차단하면_예외가_발생한다() {
            // when & then
            assertThatCode(() -> siteUserService.blockUser(user.getId(), user.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(CANNOT_BLOCK_YOURSELF.getMessage());
        }

        @Test
        void 이미_차단했으면_예외가_발생한다() {
            // given
            siteUserService.blockUser(user.getId(), blockedUser.getId());

            // when & then
            assertThatCode(() -> siteUserService.blockUser(user.getId(), blockedUser.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ALREADY_BLOCKED_BY_CURRENT_USER.getMessage());
        }
    }
}
