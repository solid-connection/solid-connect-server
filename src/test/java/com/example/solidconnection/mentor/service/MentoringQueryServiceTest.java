package com.example.solidconnection.mentor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.chat.domain.ChatRoom;
import com.example.solidconnection.chat.fixture.ChatRoomFixture;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.mentor.domain.Channel;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.mentor.dto.ChannelResponse;
import com.example.solidconnection.mentor.dto.MatchedMentorResponse;
import com.example.solidconnection.mentor.dto.MentoringForMenteeResponse;
import com.example.solidconnection.mentor.dto.MentoringForMentorResponse;
import com.example.solidconnection.mentor.fixture.ChannelFixture;
import com.example.solidconnection.mentor.fixture.MentorFixture;
import com.example.solidconnection.mentor.fixture.MentoringFixture;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.fixture.UniversityFixture;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@TestContainerSpringBootTest
@DisplayName("멘토링 조회 서비스 테스트")
class MentoringQueryServiceTest {

    @Autowired
    private MentoringQueryService mentoringQueryService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private MentorFixture mentorFixture;

    @Autowired
    private MentoringFixture mentoringFixture;

    @Autowired
    private UniversityFixture universityFixture;

    @Autowired
    private ChannelFixture channelFixture;

    @Autowired
    private MentoringRepository mentoringRepository;

    @Autowired
    private ChatRoomFixture chatRoomFixture;

    private SiteUser mentorUser1, mentorUser2;
    private SiteUser menteeUser1, menteeUser2, menteeUser3;
    private Mentor mentor1, mentor2, mentor3;
    private HostUniversity university;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        mentorUser1 = siteUserFixture.멘토(1, "mentor1");
        mentorUser2 = siteUserFixture.멘토(2, "mentor2");
        SiteUser mentorUser3 = siteUserFixture.멘토(3, "mentor3");
        menteeUser1 = siteUserFixture.사용자(1, "mentee1");
        menteeUser2 = siteUserFixture.사용자(2, "mentee2");
        menteeUser3 = siteUserFixture.사용자(3, "mentee3");
        university = universityFixture.괌_대학();
        mentor1 = mentorFixture.멘토(mentorUser1.getId(), university.getId());
        mentor2 = mentorFixture.멘토(mentorUser2.getId(), university.getId());
        mentor3 = mentorFixture.멘토(mentorUser3.getId(), university.getId());
        pageable = PageRequest.of(0, 3);
    }

    @Nested
    class 멘토의_멘토링_목록_조회_테스트 {

        @Test
        void 모든_상태의_멘토링_목록을_조회한다() {
            // given
            Mentoring mentoring1 = mentoringFixture.대기중_멘토링(mentor1.getId(), menteeUser1.getId());
            Mentoring mentoring2 = mentoringFixture.승인된_멘토링(mentor1.getId(), menteeUser2.getId());
            Mentoring mentoring3 = mentoringFixture.거절된_멘토링(mentor1.getId(), menteeUser3.getId());

            ChatRoom chatRoom2 = chatRoomFixture.멘토링_채팅방(mentoring2.getId());

            // when
            SliceResponse<MentoringForMentorResponse> response = mentoringQueryService.getMentoringsForMentor(mentorUser1.getId(), pageable);

            // then
            assertThat(response.content())
                    .extracting(MentoringForMentorResponse::verifyStatus, MentoringForMentorResponse::roomId)
                    .containsExactlyInAnyOrder(
                            tuple(VerifyStatus.PENDING, null),
                            tuple(VerifyStatus.APPROVED, chatRoom2.getId()),
                            tuple(VerifyStatus.REJECTED, null)
                    );
        }

        @Test
        void 멘토링_상대의_정보를_포함한다() {
            // given
            mentoringFixture.대기중_멘토링(mentor1.getId(), menteeUser1.getId());
            mentoringFixture.승인된_멘토링(mentor1.getId(), menteeUser2.getId());

            // when
            SliceResponse<MentoringForMentorResponse> response = mentoringQueryService.getMentoringsForMentor(mentorUser1.getId(), pageable);

            // then
            assertThat(response.content()).extracting(MentoringForMentorResponse::nickname)
                    .containsExactlyInAnyOrder(
                            menteeUser1.getNickname(),
                            menteeUser2.getNickname()
                    );
        }

        @Test
        void 멘토링_확인_여부를_포함한다() {
            // given
            Mentoring mentoring1 = mentoringFixture.대기중_멘토링(mentor1.getId(), menteeUser1.getId());
            Mentoring mentoring2 = mentoringFixture.승인된_멘토링(mentor1.getId(), menteeUser2.getId());

            // when
            SliceResponse<MentoringForMentorResponse> response = mentoringQueryService.getMentoringsForMentor(mentorUser1.getId(), pageable);

            // then
            assertThat(response.content())
                    .extracting(MentoringForMentorResponse::nickname, MentoringForMentorResponse::isChecked)
                    .containsExactlyInAnyOrder(
                            tuple(menteeUser1.getNickname(), false),
                            tuple(menteeUser2.getNickname(), true)
                    );
        }

        @Test
        void 멘토링이_없는_경우_빈_리스트를_반환한다() {
            // when
            SliceResponse<MentoringForMentorResponse> response = mentoringQueryService.getMentoringsForMentor(mentorUser1.getId(), pageable);

            // then
            assertThat(response.content()).isEmpty();
        }
    }

    @Nested
    class 멘티의_멘토링_목록_조회_테스트 {

        @Test
        void 거절된_멘토링_목록을_조회하면_예외가_발생한다() {
            // given
            mentoringFixture.거절된_멘토링(mentor1.getId(), menteeUser1.getId());

            // when & then
            assertThatCode(() -> mentoringQueryService.getMentoringsForMentee(menteeUser1.getId(), VerifyStatus.REJECTED, pageable))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.UNAUTHORIZED_MENTORING.getMessage());
        }

        @Test
        void 승인된_멘토링_목록과_대응하는_채팅방을_조회한다() {
            // given
            Mentoring mentoring1 = mentoringFixture.승인된_멘토링(mentor1.getId(), menteeUser1.getId());
            Mentoring mentoring2 = mentoringFixture.승인된_멘토링(mentor2.getId(), menteeUser1.getId());
            ChatRoom mentoringChatRoom1 = chatRoomFixture.멘토링_채팅방(mentoring1.getId());
            ChatRoom mentoringChatRoom2 = chatRoomFixture.멘토링_채팅방(mentoring2.getId());
            mentoringFixture.대기중_멘토링(mentor3.getId(), menteeUser1.getId());

            // when
            SliceResponse<MentoringForMenteeResponse> response = mentoringQueryService.getMentoringsForMentee(
                    menteeUser1.getId(), VerifyStatus.APPROVED, pageable);

            // then
            assertThat(response.content()).extracting(MentoringForMenteeResponse::mentoringId, MentoringForMenteeResponse::chatRoomId)
                    .containsExactlyInAnyOrder(
                            tuple(mentoring1.getId(), mentoringChatRoom1.getId()),
                            tuple(mentoring2.getId(), mentoringChatRoom2.getId())
                    );
        }

        @Test
        void 대기중인_멘토링_목록을_조회한다() {
            // given
            Mentoring mentoring1 = mentoringFixture.대기중_멘토링(mentor1.getId(), menteeUser1.getId());
            Mentoring mentoring2 = mentoringFixture.대기중_멘토링(mentor2.getId(), menteeUser1.getId());
            mentoringFixture.승인된_멘토링(mentor3.getId(), menteeUser1.getId());

            // when
            SliceResponse<MentoringForMenteeResponse> response = mentoringQueryService.getMentoringsForMentee(
                    menteeUser1.getId(), VerifyStatus.PENDING, pageable);

            // then
            assertThat(response.content()).extracting(MentoringForMenteeResponse::mentoringId)
                    .containsExactlyInAnyOrder(
                            mentoring1.getId(),
                            mentoring2.getId()
                    );
        }

        @Test
        void 멘토링_상대의_정보를_포함한다() {
            // given
            mentoringFixture.승인된_멘토링(mentor1.getId(), menteeUser1.getId());
            mentoringFixture.승인된_멘토링(mentor2.getId(), menteeUser1.getId());

            // when
            SliceResponse<MentoringForMenteeResponse> response = mentoringQueryService.getMentoringsForMentee(
                    menteeUser1.getId(), VerifyStatus.APPROVED, pageable);

            // then
            assertThat(response.content()).extracting(MentoringForMenteeResponse::nickname)
                    .containsExactlyInAnyOrder(
                            mentorUser1.getNickname(),
                            mentorUser2.getNickname()
                    );
        }

        @Test
        void 멘토링_확인_여부를_포함한다() {
            // given
            Mentoring mentoring1 = mentoringFixture.대기중_멘토링(mentor1.getId(), menteeUser1.getId());
            Mentoring mentoring2 = mentoringFixture.대기중_멘토링(mentor2.getId(), menteeUser1.getId());
            mentoring1.checkByMentee();
            mentoringRepository.save(mentoring1);

            // when
            SliceResponse<MentoringForMenteeResponse> response = mentoringQueryService.getMentoringsForMentee(
                    menteeUser1.getId(), VerifyStatus.PENDING, pageable);

            // then
            assertThat(response.content())
                    .extracting(MentoringForMenteeResponse::mentoringId, MentoringForMenteeResponse::isChecked)
                    .containsExactlyInAnyOrder(
                            tuple(mentoring1.getId(), true),
                            tuple(mentoring2.getId(), false)
                    );
        }

        @Test
        void 멘토링이_없는_경우_빈_리스트를_반환한다() {
            // when
            SliceResponse<MentoringForMenteeResponse> response = mentoringQueryService.getMentoringsForMentee(
                    mentorUser1.getId(), VerifyStatus.APPROVED, pageable);

            // then
            assertThat(response.content()).isEmpty();
        }
    }

    @Nested
    class 멘티의_멘토_목록_조회_테스트 {

        private static final int NO_NEXT_PAGE_NUMBER = -1;

        private Mentoring mentoring1, mentoring2;
        private ChatRoom chatRoom1, chatRoom2;

        @BeforeEach
        void setUp() {
            mentoring1 = mentoringFixture.승인된_멘토링(mentor1.getId(), menteeUser1.getId());
            mentoring2 = mentoringFixture.승인된_멘토링(mentor2.getId(), menteeUser1.getId());

            chatRoom1 = chatRoomFixture.멘토링_채팅방(mentoring1.getId());
            chatRoom2 = chatRoomFixture.멘토링_채팅방(mentoring2.getId());
        }

        @Test
        void 매칭된_멘토의_목록_정보를_조회한다() {
            // given
            Channel channel1 = channelFixture.채널(1, mentor1);
            Channel channel2 = channelFixture.채널(2, mentor2);

            // when
            SliceResponse<MatchedMentorResponse> response = mentoringQueryService.getMatchedMentors(menteeUser1.getId(), PageRequest.of(0, 10));

            // then
            Map<Long, MatchedMentorResponse> matchMentorMap = response.content().stream()
                    .collect(Collectors.toMap(MatchedMentorResponse::id, Function.identity()));
            MatchedMentorResponse mentor1Response = matchMentorMap.get(mentor1.getSiteUserId());
            MatchedMentorResponse mentor2Response = matchMentorMap.get(mentor2.getSiteUserId());
            assertAll(
                    () -> assertThat(mentor1Response.roomId()).isEqualTo(chatRoom1.getId()),
                    () -> assertThat(mentor1Response.nickname()).isEqualTo(mentorUser1.getNickname()),
                    () -> assertThat(mentor1Response.universityName()).isEqualTo(university.getKoreanName()),
                    () -> assertThat(mentor1Response.country()).isEqualTo(university.getCountry().getKoreanName()),
                    () -> assertThat(mentor1Response.channels()).extracting(ChannelResponse::url)
                            .containsOnly(channel1.getUrl()),

                    () -> assertThat(mentor2Response.roomId()).isEqualTo(chatRoom2.getId()),
                    () -> assertThat(mentor2Response.nickname()).isEqualTo(mentorUser2.getNickname()),
                    () -> assertThat(mentor2Response.universityName()).isEqualTo(university.getKoreanName()),
                    () -> assertThat(mentor2Response.country()).isEqualTo(university.getCountry().getKoreanName()),
                    () -> assertThat(mentor2Response.channels()).extracting(ChannelResponse::url)
                            .containsOnly(channel2.getUrl())
            );
        }

        @Test
        void 다음_페이지_번호를_응답한다() {
            // given
            SliceResponse<MatchedMentorResponse> response = mentoringQueryService.getMatchedMentors(menteeUser1.getId(), PageRequest.of(0, 1));

            // then
            assertThat(response.nextPageNumber()).isEqualTo(2);
        }

        @Test
        void 다음_페이지가_없으면_페이지_없음을_의미하는_값을_응답한다() {
            // given
            SliceResponse<MatchedMentorResponse> response = mentoringQueryService.getMatchedMentors(menteeUser1.getId(), PageRequest.of(0, 10));

            // then
            assertThat(response.nextPageNumber()).isEqualTo(NO_NEXT_PAGE_NUMBER);
        }
    }
}
