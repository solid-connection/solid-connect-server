package com.example.solidconnection.siteuser.service;

import static com.example.solidconnection.common.exception.ErrorCode.ALREADY_BLOCKED_BY_CURRENT_USER;
import static com.example.solidconnection.common.exception.ErrorCode.BLOCK_USER_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.CANNOT_BLOCK_YOURSELF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.domain.UserBlock;
import com.example.solidconnection.siteuser.dto.NicknameExistsResponse;
import com.example.solidconnection.siteuser.dto.UserBlockResponse;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.siteuser.fixture.UserBlockFixture;
import com.example.solidconnection.siteuser.repository.UserBlockRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@TestContainerSpringBootTest
@DisplayName("유저 서비스 테스트")
class SiteUserServiceTest {

    @Autowired
    private SiteUserService siteUserService;

    @Autowired
    private UserBlockRepository userBlockRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private UserBlockFixture userBlockFixture;

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
    class 유저_차단_조회 {

        private static final int NO_NEXT_PAGE_NUMBER = -1;

        private SiteUser blockedUser1;
        private SiteUser blockedUser2;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
            blockedUser1 = siteUserFixture.사용자(1, "blockedUser1");
            blockedUser2 = siteUserFixture.사용자(2, "blockedUser2");
            pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        @Test
        void 최신순으로_차단한_사용자를_정상적으로_조회한다() {
            // given
            UserBlock userBlock1 = userBlockFixture.유저_차단(user.getId(), blockedUser1.getId());
            UserBlock userBlock2 = userBlockFixture.유저_차단(user.getId(), blockedUser2.getId());

            // when
            SliceResponse<UserBlockResponse> response = siteUserService.getBlockedUsers(user.getId(), pageable);

            // then
            assertAll(
                    () -> assertThat(response.content()).hasSize(2),
                    () -> assertThat(response.content().get(0).id()).isEqualTo(userBlock2.getId()),
                    () -> assertThat(response.content().get(0).blockedId()).isEqualTo(blockedUser2.getId()),
                    () -> assertThat(response.content().get(1).id()).isEqualTo(userBlock1.getId()),
                    () -> assertThat(response.content().get(1).blockedId()).isEqualTo(blockedUser1.getId())
            );
        }

        @Test
        void 차단한_사용자가_없으면_빈_목록을_반환한다() {
            // when
            SliceResponse<UserBlockResponse> response = siteUserService.getBlockedUsers(user.getId(), pageable);

            // then
            assertAll(
                    () -> assertThat(response.content()).isEmpty(),
                    () -> assertThat(response.nextPageNumber()).isEqualTo(NO_NEXT_PAGE_NUMBER)
            );
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

        @Test
        void 성공적으로_유저_차단을_취소한다() {
            // given
            userBlockFixture.유저_차단(user.getId(), blockedUser.getId());

            // when
            siteUserService.cancelUserBlock(user.getId(), blockedUser.getId());

            // then
            assertThat(userBlockRepository.existsByBlockerIdAndBlockedId(user.getId(), blockedUser.getId())).isFalse();
        }

        @Test
        void 차단하지_않았으면_예외가_발생한다() {
            // when & then
            assertThatCode(() -> siteUserService.cancelUserBlock(user.getId(), blockedUser.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(BLOCK_USER_NOT_FOUND.getMessage());
        }
    }
}
