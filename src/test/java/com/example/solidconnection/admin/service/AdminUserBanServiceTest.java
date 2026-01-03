package com.example.solidconnection.admin.service;

import static com.example.solidconnection.common.exception.ErrorCode.ALREADY_BANNED_USER;
import static com.example.solidconnection.common.exception.ErrorCode.NOT_BANNED_USER;
import static com.example.solidconnection.common.exception.ErrorCode.REPORT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.admin.dto.UserBanRequest;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.community.board.fixture.BoardFixture;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostCategory;
import com.example.solidconnection.community.post.fixture.PostFixture;
import com.example.solidconnection.report.domain.TargetType;
import com.example.solidconnection.report.fixture.ReportFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.domain.UserBan;
import com.example.solidconnection.siteuser.domain.UserBanDuration;
import com.example.solidconnection.siteuser.domain.UserStatus;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.siteuser.fixture.UserBanFixture;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.siteuser.repository.UserBanRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("어드민 유저 차단 서비스 테스트")
class AdminUserBanServiceTest {

    @Autowired
    private AdminUserBanService adminUserBanService;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private UserBanRepository userBanRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private UserBanFixture userBanFixture;

    @Autowired
    private ReportFixture reportFixture;

    @Autowired
    private PostFixture postFixture;

    @Autowired
    private BoardFixture boardFixture;

    private SiteUser admin;
    private SiteUser reportedUser;
    private SiteUser reporter;
    private Post reportedPost;

    @BeforeEach
    void setUp() {
        admin = siteUserFixture.관리자();
        reportedUser = siteUserFixture.신고된_사용자("신고된사용자");
        reporter = siteUserFixture.사용자(2, "신고자");
        reportedPost = postFixture.게시글(
                "신고될 게시글",
                "신고될 내용",
                false,
                PostCategory.자유,
                boardFixture.자유게시판(),
                reportedUser
        );
    }

    @Nested
    @DisplayName("사용자 차단")
    class 사용자_차단 {

        @Test
        void 사용자를_차단한다() {
            // given
            reportFixture.신고(reporter.getId(), reportedUser.getId(), TargetType.POST, reportedPost.getId());
            UserBanRequest request = new UserBanRequest(UserBanDuration.SEVEN_DAYS);

            // when
            adminUserBanService.banUser(reportedUser.getId(), request);

            // then
            SiteUser bannedUser = siteUserRepository.findById(reportedUser.getId()).orElseThrow();
            assertThat(bannedUser.getUserStatus()).isEqualTo(UserStatus.BANNED);
        }

        @Test
        void 이미_차단된_사용자는_다시_차단할_수_없다() {
            // given
            reportFixture.신고(reporter.getId(), reportedUser.getId(), TargetType.POST, reportedPost.getId());
            UserBanRequest request = new UserBanRequest(UserBanDuration.SEVEN_DAYS);
            adminUserBanService.banUser(reportedUser.getId(), request);

            // when & then
            assertThatCode(() -> adminUserBanService.banUser(reportedUser.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ALREADY_BANNED_USER.getMessage());
        }

        @Test
        void 신고가_없는_사용자는_차단할_수_없다() {
            // given
            SiteUser userWithoutReport = siteUserFixture.사용자(3, "신고없는유저");
            UserBanRequest request = new UserBanRequest(UserBanDuration.SEVEN_DAYS);

            // when & then
            assertThatCode(() -> adminUserBanService.banUser(userWithoutReport.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(REPORT_NOT_FOUND.getMessage());
        }

        @Nested
        class 사용자_차단_해제 {

            @Test
            void 차단된_사용자를_수동으로_해제한다() {
                // given
                reportFixture.신고(reporter.getId(), reportedUser.getId(), TargetType.POST, reportedPost.getId());
                UserBanRequest request = new UserBanRequest(UserBanDuration.SEVEN_DAYS);
                adminUserBanService.banUser(reportedUser.getId(), request);

                // when
                adminUserBanService.unbanUser(reportedUser.getId(), admin.getId());

                // then
                SiteUser unbannedUser = siteUserRepository.findById(reportedUser.getId()).orElseThrow();
                assertThat(unbannedUser.getUserStatus()).isEqualTo(UserStatus.REPORTED);
            }

            @Test
            void 차단_해제_정보가_올바르게_저장된다() {
                // given
                reportFixture.신고(reporter.getId(), reportedUser.getId(), TargetType.POST, reportedPost.getId());
                UserBanRequest request = new UserBanRequest(UserBanDuration.SEVEN_DAYS);
                adminUserBanService.banUser(reportedUser.getId(), request);
                ZonedDateTime beforeUnban = ZonedDateTime.now();

                // when
                adminUserBanService.unbanUser(reportedUser.getId(), admin.getId());

                // then
                List<UserBan> allBans = userBanRepository.findAll();
                UserBan unbannedUserBan = allBans.stream()
                        .filter(ban -> ban.getBannedUserId().equals(reportedUser.getId()))
                        .findFirst()
                        .orElseThrow();

                assertAll(
                        () -> assertThat(unbannedUserBan.isUnbanned()).isTrue(),
                        () -> assertThat(unbannedUserBan.getUnbannedBy()).isEqualTo(admin.getId()),
                        () -> assertThat(unbannedUserBan.getUnbannedAt()).isAfter(beforeUnban)
                );
            }

            @Test
            void 차단되지_않은_사용자는_차단_해제할_수_없다() {
                // given
                SiteUser notBannedUser = siteUserFixture.사용자(3, "차단안된유저");

                // when & then
                assertThatCode(() -> adminUserBanService.unbanUser(notBannedUser.getId(), admin.getId()))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(NOT_BANNED_USER.getMessage());
            }

            @Test
            void 만료된_차단은_해제할_수_없다() {
                // given
                userBanFixture.만료된_차단(reportedUser.getId());

                // when & then
                assertThatCode(() -> adminUserBanService.unbanUser(reportedUser.getId(), admin.getId()))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(NOT_BANNED_USER.getMessage());
            }
        }

        @Nested
        @DisplayName("만료된 차단 자동 해제")
        class 만료된_차단_자동_해제 {

            @Test
            void 만료된_차단들을_자동으로_해제한다() {
                // given
                SiteUser user1 = siteUserFixture.사용자(10, "유저1");
                SiteUser user2 = siteUserFixture.사용자(11, "유저2");

                userBanFixture.만료된_차단(user1.getId());
                userBanFixture.만료된_차단(user2.getId());

                user1.updateUserStatus(UserStatus.BANNED);
                user2.updateUserStatus(UserStatus.BANNED);

                // when
                adminUserBanService.expireUserBans();

                // then
                SiteUser unbannedUser1 = siteUserRepository.findById(user1.getId()).orElseThrow();
                SiteUser unbannedUser2 = siteUserRepository.findById(user2.getId()).orElseThrow();

                assertAll(
                        () -> assertThat(unbannedUser1.getUserStatus()).isEqualTo(UserStatus.REPORTED),
                        () -> assertThat(unbannedUser2.getUserStatus()).isEqualTo(UserStatus.REPORTED)
                );
            }

            @Test
            void 만료되지_않은_차단은_유지된다() {
                // given
                Post reportedPost = postFixture.게시글(
                        "신고될 게시글",
                        "신고될 내용",
                        false,
                        PostCategory.자유,
                        boardFixture.자유게시판(),
                        reportedUser
                );
                reportFixture.신고(reporter.getId(), reportedUser.getId(), TargetType.POST, reportedPost.getId());
                adminUserBanService.banUser(reportedUser.getId(), new UserBanRequest(UserBanDuration.SEVEN_DAYS));

                // when
                adminUserBanService.expireUserBans();

                // then
                SiteUser stillBannedUser = siteUserRepository.findById(reportedUser.getId()).orElseThrow();
                assertThat(stillBannedUser.getUserStatus()).isEqualTo(UserStatus.BANNED);
            }

            @Test
            void 이미_수동으로_해제된_차단은_처리하지_않는다() {
                // given
                userBanFixture.수동_차단_해제(reportedUser.getId(), admin.getId());
                reportedUser.updateUserStatus(UserStatus.REPORTED);

                long beforeUnbannedCount = userBanRepository.findAll().stream()
                        .filter(UserBan::isUnbanned)
                        .count();

                // when
                adminUserBanService.expireUserBans();

                // then
                long afterUnbannedCount = userBanRepository.findAll().stream()
                        .filter(UserBan::isUnbanned)
                        .count();
                assertThat(afterUnbannedCount).isEqualTo(beforeUnbannedCount);
            }
        }
    }
}
