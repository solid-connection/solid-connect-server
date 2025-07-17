package com.example.solidconnection.mentor.service;

import static com.example.solidconnection.common.exception.ErrorCode.MENTORING_ALREADY_CONFIRMED;
import static com.example.solidconnection.common.exception.ErrorCode.MENTORING_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.REJECTED_REASON_REQUIRED;
import static com.example.solidconnection.common.exception.ErrorCode.UNAUTHORIZED_MENTORING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.mentor.dto.MentoringApplyRequest;
import com.example.solidconnection.mentor.dto.MentoringApplyResponse;
import com.example.solidconnection.mentor.dto.MentoringCheckResponse;
import com.example.solidconnection.mentor.dto.MentoringConfirmRequest;
import com.example.solidconnection.mentor.dto.MentoringConfirmResponse;
import com.example.solidconnection.mentor.fixture.MentorFixture;
import com.example.solidconnection.mentor.fixture.MentoringFixture;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("멘토링 CUD 서비스 테스트")
class MentoringCommandServiceTest {

    @Autowired
    private MentoringCommandService mentoringCommandService;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private MentoringRepository mentoringRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private MentorFixture mentorFixture;

    @Autowired
    private MentoringFixture mentoringFixture;

    private SiteUser mentorUser1;
    private SiteUser mentorUser2;

    private SiteUser menteeUser;
    private Mentor mentor1;
    private Mentor mentor2;

    @BeforeEach
    void setUp() {
        mentorUser1 = siteUserFixture.멘토(1, "mentor1");
        menteeUser = siteUserFixture.사용자(2, "mentee1");
        mentorUser2 = siteUserFixture.멘토(3, "mentor2");

        mentor1 = mentorFixture.멘토(mentorUser1.getId(), 1L);
        mentor2 = mentorFixture.멘토(mentorUser2.getId(), 2L);
    }

    @Nested
    class 멘토링_신청_테스트 {

        @Test
        void 멘토링을_성공적으로_신청한다() {
            // given
            MentoringApplyRequest request = new MentoringApplyRequest(mentor1.getId());

            // when
            MentoringApplyResponse response = mentoringCommandService.applyMentoring(menteeUser.getId(), request);

            // then
            Mentoring mentoring = mentoringRepository.findById(response.mentoringId()).orElseThrow();

            assertAll(
                    () -> assertThat(mentoring.getMentorId()).isEqualTo(mentor1.getId()),
                    () -> assertThat(mentoring.getMenteeId()).isEqualTo(menteeUser.getId()),
                    () -> assertThat(mentoring.getVerifyStatus()).isEqualTo(VerifyStatus.PENDING)
            );
        }
    }

    @Nested
    class 멘토링_승인_거절_테스트 {

        @Test
        void 멘토링을_성공적으로_승인한다() {
            // given
            Mentoring mentoring = mentoringFixture.대기중_멘토링(mentor1.getId(), menteeUser.getId());
            MentoringConfirmRequest request = new MentoringConfirmRequest(VerifyStatus.APPROVED, null);
            int beforeMenteeCount = mentor1.getMenteeCount();

            // when
            MentoringConfirmResponse response = mentoringCommandService.confirmMentoring(mentorUser1.getId(), mentoring.getId(), request);

            // then
            Mentoring confirmedMentoring = mentoringRepository.findById(response.mentoringId()).orElseThrow();
            Mentor mentor = mentorRepository.findById(mentor1.getId()).orElseThrow();

            assertAll(
                    () -> assertThat(confirmedMentoring.getVerifyStatus()).isEqualTo(VerifyStatus.APPROVED),
                    () -> assertThat(confirmedMentoring.getConfirmedAt()).isNotNull(),
                    () -> assertThat(confirmedMentoring.getCheckedAt()).isNotNull(),
                    () -> assertThat(mentor.getMenteeCount()).isEqualTo(beforeMenteeCount + 1)
            );
        }

        @Test
        void 멘토링을_성공적으로_거절한다() {
            // given
            Mentoring mentoring = mentoringFixture.대기중_멘토링(mentor1.getId(), menteeUser.getId());
            String rejectedReason = "멘토링 거절 사유";
            MentoringConfirmRequest request = new MentoringConfirmRequest(VerifyStatus.REJECTED, rejectedReason);
            int beforeMenteeCount = mentor1.getMenteeCount();

            // when
            MentoringConfirmResponse response = mentoringCommandService.confirmMentoring(mentorUser1.getId(), mentoring.getId(), request);

            // then
            Mentoring confirmedMentoring = mentoringRepository.findById(response.mentoringId()).orElseThrow();
            Mentor mentor = mentorRepository.findById(mentor1.getId()).orElseThrow();

            assertAll(
                    () -> assertThat(confirmedMentoring.getVerifyStatus()).isEqualTo(VerifyStatus.REJECTED),
                    () -> assertThat(confirmedMentoring.getRejectedReason()).isEqualTo(rejectedReason),
                    () -> assertThat(confirmedMentoring.getConfirmedAt()).isNotNull(),
                    () -> assertThat(confirmedMentoring.getCheckedAt()).isNotNull(),
                    () -> assertThat(mentor.getMenteeCount()).isEqualTo(beforeMenteeCount)
            );
        }

        @Test
        void 거절_시_사유가_없으면_예외가_발생한다() {
            // given
            Mentoring mentoring = mentoringFixture.대기중_멘토링(mentor1.getId(), menteeUser.getId());
            MentoringConfirmRequest request = new MentoringConfirmRequest(VerifyStatus.REJECTED, null);

            // when & then
            assertThatThrownBy(() ->
                                       mentoringCommandService.confirmMentoring(mentorUser1.getId(), mentoring.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(REJECTED_REASON_REQUIRED.getMessage());
        }

        @Test
        void 다른_멘토의_멘토링을_승인할_수_없다() {
            // given
            Mentoring mentoring = mentoringFixture.대기중_멘토링(mentor1.getId(), menteeUser.getId());
            MentoringConfirmRequest request = new MentoringConfirmRequest(VerifyStatus.APPROVED, null);

            // when & then
            assertThatThrownBy(() -> mentoringCommandService.confirmMentoring(mentorUser2.getId(), mentoring.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UNAUTHORIZED_MENTORING.getMessage());
        }

        @Test
        void 이미_처리된_멘토링은_다시_승인할_수_없다() {
            // given
            Mentoring mentoring = mentoringFixture.승인된_멘토링(mentor1.getId(), menteeUser.getId());
            MentoringConfirmRequest request = new MentoringConfirmRequest(VerifyStatus.APPROVED, null);

            // when & then
            assertThatThrownBy(() -> mentoringCommandService.confirmMentoring(mentorUser1.getId(), mentoring.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTORING_ALREADY_CONFIRMED.getMessage());
        }

        @Test
        void 존재하지_않는_멘토링_아이디로_요청시_예외가_발생한다() {
            // given
            MentoringConfirmRequest request = new MentoringConfirmRequest(VerifyStatus.APPROVED, null);
            long invalidMentoringId = 9999L;

            // when & then
            assertThatThrownBy(() -> mentoringCommandService.confirmMentoring(mentorUser1.getId(), invalidMentoringId, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTORING_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 멘토링_확인_테스트 {

        @Test
        void 멘토링을_성공적으로_확인_처리한다() {
            // given
            Mentoring mentoring = mentoringFixture.확인되지_않은_멘토링(mentor1.getId(), menteeUser.getId());

            // when
            MentoringCheckResponse response = mentoringCommandService.checkMentoring(mentorUser1.getId(), mentoring.getId());

            // then
            Mentoring checked = mentoringRepository.findById(response.mentoringId()).orElseThrow();

            assertThat(checked.getCheckedAt()).isNotNull();
        }

        @Test
        void 다른_멘토의_멘토링은_확인할_수_없다() {
            // given
            Mentoring mentoring = mentoringFixture.확인되지_않은_멘토링(mentor1.getId(), menteeUser.getId());

            // when & then
            assertThatThrownBy(() -> mentoringCommandService.checkMentoring(mentorUser2.getId(), mentoring.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UNAUTHORIZED_MENTORING.getMessage());
        }

        @Test
        void 존재하지_않는_멘토링_아이디로_요청시_예외가_발생한다() {
            // given
            long invalidMentoringId = 9999L;

            // when & then
            assertThatThrownBy(() -> mentoringCommandService.checkMentoring(mentorUser1.getId(), invalidMentoringId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTORING_NOT_FOUND.getMessage());
        }
    }
}
