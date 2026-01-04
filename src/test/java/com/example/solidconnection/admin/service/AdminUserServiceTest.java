package com.example.solidconnection.admin.service;

import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.admin.dto.RestrictedUserInfoDetailResponse;
import com.example.solidconnection.admin.dto.RestrictedUserSearchCondition;
import com.example.solidconnection.admin.dto.RestrictedUserSearchResponse;
import com.example.solidconnection.admin.dto.UserInfoDetailResponse;
import com.example.solidconnection.admin.dto.UserSearchCondition;
import com.example.solidconnection.admin.dto.UserSearchResponse;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.application.fixture.ApplicationFixture;
import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.community.board.fixture.BoardFixture;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostCategory;
import com.example.solidconnection.community.post.fixture.PostFixture;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.UniversitySelectType;
import com.example.solidconnection.mentor.fixture.MentorApplicationFixture;
import com.example.solidconnection.mentor.fixture.MentorFixture;
import com.example.solidconnection.mentor.fixture.MentoringFixture;
import com.example.solidconnection.report.domain.TargetType;
import com.example.solidconnection.report.fixture.ReportFixture;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.domain.UserStatus;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.siteuser.fixture.UserBanFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.term.fixture.TermFixture;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.fixture.UnivApplyInfoFixture;
import com.example.solidconnection.university.fixture.UniversityFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@TestContainerSpringBootTest
@DisplayName("어드민 유저 관리 서비스 테스트")
public class AdminUserServiceTest {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private ReportFixture reportFixture;

    @Autowired
    private UserBanFixture userBanFixture;

    @Autowired
    private PostFixture postFixture;

    @Autowired
    private BoardFixture boardFixture;

    @Autowired
    private MentorFixture mentorFixture;

    @Autowired
    private MentorApplicationFixture mentorApplicationFixture;

    @Autowired
    private MentoringFixture mentoringFixture;

    @Autowired
    private UniversityFixture universityFixture;

    @Autowired
    private UnivApplyInfoFixture univApplyInfoFixture;

    @Autowired
    private ApplicationFixture applicationFixture;

    @Autowired
    private TermFixture termFixture;

    @Nested
    @DisplayName("전체 유저 검색")
    class 전체_유저_검색 {

        @Test
        void 전체_유저를_조회한다() {
            // given
            siteUserFixture.사용자(1, "유저1");
            siteUserFixture.사용자(2, "유저2");
            siteUserFixture.사용자(3, "유저3");

            UserSearchCondition condition = new UserSearchCondition(null, null);
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<UserSearchResponse> result = adminUserService.searchAllUsers(condition, pageable);

            // then
            assertThat(result.getContent()).hasSize(3);
        }

        @Test
        void role로_필터링하여_조회한다() {
            // given
            siteUserFixture.사용자(1, "멘티1");
            siteUserFixture.사용자(2, "멘티2");
            siteUserFixture.멘토(1, "멘토1");

            UserSearchCondition condition = new UserSearchCondition(Role.MENTEE, null);
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<UserSearchResponse> result = adminUserService.searchAllUsers(condition, pageable);

            // then
            assertAll(
                    () -> assertThat(result.getContent()).hasSize(2),
                    () -> assertThat(result.getContent())
                            .allMatch(user -> user.role() == Role.MENTEE)
            );
        }

        @Test
        void 닉네임으로_검색한다() {
            // given
            siteUserFixture.사용자(1, "피카츄1");
            siteUserFixture.사용자(2, "꼬부기");
            siteUserFixture.사용자(3, "피카츄2");

            UserSearchCondition condition = new UserSearchCondition(null, "피카");
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<UserSearchResponse> result = adminUserService.searchAllUsers(condition, pageable);

            // then
            assertAll(
                    () -> assertThat(result.getContent()).hasSize(2),
                    () -> assertThat(result.getContent())
                            .allMatch(user -> user.nickname().contains("피카"))
            );
        }

        @Test
        void 페이징이_정상_작동한다() {
            // given
            for (int i = 1; i <= 15; i++) {
                siteUserFixture.사용자(i, "유저" + i);
            }

            UserSearchCondition condition = new UserSearchCondition(null, null);
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<UserSearchResponse> result = adminUserService.searchAllUsers(condition, pageable);

            // then
            assertAll(
                    () -> assertThat(result.getContent()).hasSize(10),
                    () -> assertThat(result.getTotalElements()).isEqualTo(15),
                    () -> assertThat(result.getTotalPages()).isEqualTo(2)
            );
        }
    }

    @Nested
    @DisplayName("유저 상세 정보 조회")
    class 유저_상세_정보_조회 {

        @Test
        void 멘티_유저_상세_정보를_조회한다() {
            // given
            SiteUser mentee = siteUserFixture.사용자(1, "멘티유저");
            long termId = termFixture.현재_학기("2025-1").getId();

            UnivApplyInfo firstChoice = univApplyInfoFixture.괌대학_A_지원_정보(termId);
            UnivApplyInfo secondChoice = univApplyInfoFixture.네바다주립대학_라스베이거스_지원_정보(termId);

            applicationFixture.지원서(
                    mentee,
                    "지원닉네임",
                    termId,
                    new Gpa(4.0, 4.5, "http://gpa-report.com/test.pdf"),
                    new LanguageTest(LanguageTestType.TOEIC, "900", "http://language-test.com/test.pdf"),
                    firstChoice.getId(),
                    secondChoice.getId(),
                    null
            );

            // when
            UserInfoDetailResponse result = adminUserService.getUserInfoDetail(mentee.getId());

            // then
            assertAll(
                    () -> assertThat(result.mentorInfoResponse()).isNull(),
                    () -> assertThat(result.menteeInfoResponse()).isNotNull(),
                    () -> assertThat(result.menteeInfoResponse().univApplyInfos()).isNotNull()
            );
        }

        @Test
        void 멘토_유저_상세_정보를_조회한다() {
            // given
            SiteUser mentorUser = siteUserFixture.멘토(1, "멘토유저");
            University university = universityFixture.괌_대학();

            Mentor mentor = mentorFixture.멘토(mentorUser.getId(), university.getId());
            mentorApplicationFixture.승인된_멘토신청(
                    mentorUser.getId(),
                    UniversitySelectType.CATALOG,
                    university.getId()
            );

            SiteUser mentee = siteUserFixture.사용자(1, "멘티유저");
            mentoringFixture.승인된_멘토링(mentor.getId(), mentee.getId());

            // when
            UserInfoDetailResponse result = adminUserService.getUserInfoDetail(mentorUser.getId());

            // then
            assertAll(
                    () -> assertThat(result.mentorInfoResponse()).isNotNull(),
                    () -> assertThat(result.menteeInfoResponse()).isNull(),
                    () -> assertThat(result.mentorInfoResponse().menteeInfos()).hasSize(1),
                    () -> assertThat(result.mentorInfoResponse().mentorApplicationHistory()).hasSize(1)
            );
        }

        @Test
        void 신고된_유저는_신고_내역이_포함된다() {
            // given
            SiteUser reportedUser = siteUserFixture.신고된_사용자("신고된유저");
            SiteUser reporter = siteUserFixture.사용자(1, "신고자");

            Post post = postFixture.게시글(
                    "신고된 게시글",
                    "내용",
                    false,
                    PostCategory.자유,
                    boardFixture.자유게시판(),
                    reportedUser
            );

            reportFixture.신고(reporter.getId(), reportedUser.getId(), TargetType.POST, post.getId());

            // when
            UserInfoDetailResponse result = adminUserService.getUserInfoDetail(reportedUser.getId());

            // then
            assertThat(result.reportedHistoryResponses()).hasSize(1);
        }

        @Test
        void 존재하지_않는_유저_조회_시_예외_응답을_반환한다() {
            // given
            long notExistUserId = 999999L;

            // when & then
            assertThatCode(() -> adminUserService.getUserInfoDetail(notExistUserId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(USER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("신고/차단된 유저 검색")
    class 신고_차단된_유저_검색 {

        @Test
        void 신고_차단된_유저만_조회한다() {
            // given
            siteUserFixture.사용자(1, "일반유저");
            siteUserFixture.신고된_사용자("신고된유저");
            siteUserFixture.차단된_사용자("차단된유저");

            RestrictedUserSearchCondition condition = new RestrictedUserSearchCondition(null, null, null);
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<RestrictedUserSearchResponse> result = adminUserService.searchRestrictedUsers(condition, pageable);

            // then
            assertAll(
                    () -> assertThat(result.getContent()).hasSize(2),
                    () -> assertThat(result.getContent())
                            .allMatch(user ->
                                    user.userStatus() == UserStatus.REPORTED ||
                                    user.userStatus() == UserStatus.BANNED
                            )
            );
        }

        @Test
        void role로_필터링하여_조회한다() {
            // given
            siteUserFixture.신고된_사용자("신고된멘티");
            siteUserFixture.신고된_사용자_멘토(1, "신고된멘토");

            RestrictedUserSearchCondition condition = new RestrictedUserSearchCondition(Role.MENTOR, null, null);
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<RestrictedUserSearchResponse> result = adminUserService.searchRestrictedUsers(condition, pageable);

            // then
            assertAll(
                    () -> assertThat(result.getContent()).hasSize(1),
                    () -> assertThat(result.getContent().get(0).role()).isEqualTo(Role.MENTOR)
            );
        }

        @Test
        void userStatus로_필터링하여_조회한다() {
            // given
            siteUserFixture.신고된_사용자("신고된유저");
            siteUserFixture.차단된_사용자("차단된유저");

            RestrictedUserSearchCondition condition = new RestrictedUserSearchCondition(null, UserStatus.BANNED, null);
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<RestrictedUserSearchResponse> result = adminUserService.searchRestrictedUsers(condition, pageable);

            // then
            assertAll(
                    () -> assertThat(result.getContent()).hasSize(1),
                    () -> assertThat(result.getContent().get(0).userStatus()).isEqualTo(UserStatus.BANNED)
            );
        }

        @Test
        void 닉네임으로_검색한다() {
            // given
            siteUserFixture.신고된_사용자("피카츄");
            siteUserFixture.차단된_사용자("꼬부기");

            RestrictedUserSearchCondition condition = new RestrictedUserSearchCondition(null, null, "피카");
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<RestrictedUserSearchResponse> result = adminUserService.searchRestrictedUsers(condition, pageable);

            // then
            assertAll(
                    () -> assertThat(result.getContent()).hasSize(1),
                    () -> assertThat(result.getContent().get(0).nickname()).contains("피카")
            );
        }
    }

    @Nested
    @DisplayName("신고/차단된 유저 상세 정보 조회")
    class 신고_차단된_유저_상세_정보_조회 {

        @Test
        void 신고_내역을_조회한다() {
            // given
            SiteUser reportedUser = siteUserFixture.신고된_사용자("신고된유저");
            SiteUser reporter1 = siteUserFixture.사용자(1, "신고자1");
            SiteUser reporter2 = siteUserFixture.사용자(2, "신고자2");

            Post post = postFixture.게시글(
                    "게시글",
                    "내용",
                    false,
                    PostCategory.자유,
                    boardFixture.자유게시판(),
                    reportedUser
            );

            reportFixture.신고(reporter1.getId(), reportedUser.getId(), TargetType.POST, post.getId());
            reportFixture.신고(reporter2.getId(), reportedUser.getId(), TargetType.POST, post.getId());

            // when
            RestrictedUserInfoDetailResponse result = adminUserService.getRestrictedUserInfoDetail(reportedUser.getId());

            // then
            assertThat(result.reportedHistoryResponses()).hasSize(2);
        }

        @Test
        void 차단_내역을_조회한다() {
            // given
            SiteUser bannedUser = siteUserFixture.차단된_사용자("차단된유저");
            SiteUser admin = siteUserFixture.관리자();

            userBanFixture.수동_차단_해제(bannedUser.getId(), admin.getId());

            // when
            RestrictedUserInfoDetailResponse result = adminUserService.getRestrictedUserInfoDetail(bannedUser.getId());

            // then
            assertThat(result.bannedHistoryResponses()).hasSize(1);
        }

        @Test
        void 신고_차단_내역을_함께_조회한다() {
            // given
            SiteUser user = siteUserFixture.차단된_사용자("차단된유저");
            SiteUser reporter = siteUserFixture.사용자(1, "신고자");
            SiteUser admin = siteUserFixture.관리자();

            Post post = postFixture.게시글(
                    "게시글",
                    "내용",
                    false,
                    PostCategory.자유,
                    boardFixture.자유게시판(),
                    user
            );

            reportFixture.신고(reporter.getId(), user.getId(), TargetType.POST, post.getId());
            userBanFixture.수동_차단_해제(user.getId(), admin.getId());

            // when
            RestrictedUserInfoDetailResponse result = adminUserService.getRestrictedUserInfoDetail(user.getId());

            // then
            assertAll(
                    () -> assertThat(result.reportedHistoryResponses()).hasSize(1),
                    () -> assertThat(result.bannedHistoryResponses()).hasSize(1)
            );
        }

        @Test
        void 존재하지_않는_유저_조회_시_예외_응답을_반환한다() {
            // given
            long notExistUserId = 999999L;

            // when & then
            assertThatCode(() -> adminUserService.getRestrictedUserInfoDetail(notExistUserId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(USER_NOT_FOUND.getMessage());
        }
    }
}
