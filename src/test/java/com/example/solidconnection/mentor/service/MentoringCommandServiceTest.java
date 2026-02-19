package com.example.solidconnection.mentor.service;

import static com.example.solidconnection.common.exception.ErrorCode.ALREADY_EXIST_MENTORING;
import static com.example.solidconnection.common.exception.ErrorCode.MENTORING_ALREADY_CONFIRMED;
import static com.example.solidconnection.common.exception.ErrorCode.MENTORING_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.UNAUTHORIZED_MENTORING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.awaitility.Awaitility.await;
import com.example.solidconnection.chat.domain.ChatParticipant;
import com.example.solidconnection.chat.domain.ChatRoom;
import com.example.solidconnection.chat.repository.ChatRoomRepositoryForTest;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.mentor.dto.MentoringApplyRequest;
import com.example.solidconnection.mentor.dto.MentoringApplyResponse;
import com.example.solidconnection.mentor.dto.MentoringConfirmRequest;
import com.example.solidconnection.mentor.dto.MentoringConfirmResponse;
import com.example.solidconnection.mentor.fixture.MentorFixture;
import com.example.solidconnection.mentor.fixture.MentoringFixture;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
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
    private ChatRoomRepositoryForTest chatRoomRepositoryForTest;

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
            MentoringApplyRequest request = new MentoringApplyRequest(mentor1.getSiteUserId());

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

        @Test
        void 동일_멘티_멘토끼리는_재신청되지않는다() {
            // given
            mentoringFixture.대기중_멘토링(mentor1.getId(), menteeUser.getId());
            MentoringApplyRequest request = new MentoringApplyRequest(mentor1.getSiteUserId());

            // when & then
            assertThatThrownBy(() -> mentoringCommandService.applyMentoring(menteeUser.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ALREADY_EXIST_MENTORING.getMessage());
        }
    }

    @Nested
    class 멘토링_승인_거절_테스트 {

        @Test
        void 멘토링을_성공적으로_승인한다() {
            // given
            Mentoring mentoring = mentoringFixture.대기중_멘토링(mentor1.getId(), menteeUser.getId());
            MentoringConfirmRequest request = new MentoringConfirmRequest(VerifyStatus.APPROVED);
            int beforeMenteeCount = mentor1.getMenteeCount();

            // when
            MentoringConfirmResponse response = mentoringCommandService.confirmMentoring(mentorUser1.getId(), mentoring.getId(), request);

            // then
            Mentoring confirmedMentoring = mentoringRepository.findById(response.mentoringId()).orElseThrow();
            Mentor mentor = mentorRepository.findById(mentor1.getId()).orElseThrow();

            assertAll(
                    () -> assertThat(confirmedMentoring.getVerifyStatus()).isEqualTo(VerifyStatus.APPROVED),
                    () -> assertThat(confirmedMentoring.getConfirmedAt()).isNotNull(),
                    () -> assertThat(confirmedMentoring.getCheckedAtByMentor()).isNotNull(),
                    () -> assertThat(confirmedMentoring.getCheckedAtByMentee()).isNull(),
                    () -> assertThat(mentor.getMenteeCount()).isEqualTo(beforeMenteeCount + 1)
            );
        }

        @Test
        void 멘토링_승인시_채팅방이_자동으로_생성된다() {
            // given
            Mentoring mentoring = mentoringFixture.대기중_멘토링(mentor1.getId(), menteeUser.getId());
            MentoringConfirmRequest request = new MentoringConfirmRequest(VerifyStatus.APPROVED);

            Optional<ChatRoom> beforeChatRoom = chatRoomRepositoryForTest.findOneOnOneChatRoomByParticipants(mentorUser1.getId(), menteeUser.getId());
            assertThat(beforeChatRoom).isEmpty();

            // when
            MentoringConfirmResponse response = mentoringCommandService.confirmMentoring(mentorUser1.getId(), mentoring.getId(), request);

            // then
            ChatRoom afterChatRoom = chatRoomRepositoryForTest.findOneOnOneChatRoomByParticipants(mentorUser1.getId(), menteeUser.getId()).orElseThrow();
            List<Long> participantIds = afterChatRoom.getChatParticipants().stream()
                    .map(ChatParticipant::getSiteUserId)
                    .toList();
            assertAll(
                    () -> assertThat(afterChatRoom.isGroup()).isFalse(),
                    () -> assertThat(participantIds).containsExactly(mentorUser1.getId(), menteeUser.getId()),
                    () -> assertThat(response.chatRoomId()).isEqualTo(afterChatRoom.getId())
            );
        }

        @Test
        void 멘토링을_성공적으로_거절한다() {
            // given
            Mentoring mentoring = mentoringFixture.대기중_멘토링(mentor1.getId(), menteeUser.getId());
            MentoringConfirmRequest request = new MentoringConfirmRequest(VerifyStatus.REJECTED);
            int beforeMenteeCount = mentor1.getMenteeCount();

            // when
            MentoringConfirmResponse response = mentoringCommandService.confirmMentoring(mentorUser1.getId(), mentoring.getId(), request);

            // then
            Mentoring confirmedMentoring = mentoringRepository.findById(response.mentoringId()).orElseThrow();
            Mentor mentor = mentorRepository.findById(mentor1.getId()).orElseThrow();

            assertAll(
                    () -> assertThat(confirmedMentoring.getVerifyStatus()).isEqualTo(VerifyStatus.REJECTED),
                    () -> assertThat(confirmedMentoring.getConfirmedAt()).isNotNull(),
                    () -> assertThat(confirmedMentoring.getCheckedAtByMentor()).isNotNull(),
                    () -> assertThat(mentor.getMenteeCount()).isEqualTo(beforeMenteeCount)
            );
        }

        @Test
        void 멘토링_거절시_채팅방이_자동으로_생성되지_않는다() {
            // given
            Mentoring mentoring = mentoringFixture.대기중_멘토링(mentor1.getId(), menteeUser.getId());
            MentoringConfirmRequest request = new MentoringConfirmRequest(VerifyStatus.REJECTED);

            Optional<ChatRoom> beforeChatRoom = chatRoomRepositoryForTest.findOneOnOneChatRoomByParticipants(mentorUser1.getId(), menteeUser.getId());
            assertThat(beforeChatRoom).isEmpty();

            // when
            MentoringConfirmResponse response = mentoringCommandService.confirmMentoring(mentorUser1.getId(), mentoring.getId(), request);

            // then
            Optional<ChatRoom> afterChatRoom = chatRoomRepositoryForTest.findOneOnOneChatRoomByParticipants(mentorUser1.getId(), menteeUser.getId());
            assertAll(
                    () -> assertThat(response.chatRoomId()).isNull(),
                    () -> assertThat(afterChatRoom).isEmpty()
            );
        }

        @Test
        void 다른_멘토의_멘토링을_승인할_수_없다() {
            // given
            Mentoring mentoring = mentoringFixture.대기중_멘토링(mentor1.getId(), menteeUser.getId());
            MentoringConfirmRequest request = new MentoringConfirmRequest(VerifyStatus.APPROVED);

            // when & then
            assertThatThrownBy(() -> mentoringCommandService.confirmMentoring(mentorUser2.getId(), mentoring.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UNAUTHORIZED_MENTORING.getMessage());
        }

        @Test
        void 이미_처리된_멘토링은_다시_승인할_수_없다() {
            // given
            Mentoring mentoring = mentoringFixture.승인된_멘토링(mentor1.getId(), menteeUser.getId());
            MentoringConfirmRequest request = new MentoringConfirmRequest(VerifyStatus.APPROVED);

            // when & then
            assertThatThrownBy(() -> mentoringCommandService.confirmMentoring(mentorUser1.getId(), mentoring.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTORING_ALREADY_CONFIRMED.getMessage());
        }

        @Test
        void 존재하지_않는_멘토링_아이디로_요청시_예외가_발생한다() {
            // given
            MentoringConfirmRequest request = new MentoringConfirmRequest(VerifyStatus.APPROVED);
            long invalidMentoringId = 9999L;

            // when & then
            assertThatThrownBy(() -> mentoringCommandService.confirmMentoring(mentorUser1.getId(), invalidMentoringId, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTORING_NOT_FOUND.getMessage());
        }
    }
}
