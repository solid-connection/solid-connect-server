package com.example.solidconnection.mentor.service;

import static com.example.solidconnection.common.exception.ErrorCode.CHANNEL_REGISTRATION_LIMIT_EXCEEDED;
import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_ALREADY_EXISTS;
import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_APPLICATION_NOT_FOUND;
import static com.example.solidconnection.mentor.domain.ChannelType.BLOG;
import static com.example.solidconnection.mentor.domain.ChannelType.BRUNCH;
import static com.example.solidconnection.mentor.domain.ChannelType.INSTAGRAM;
import static com.example.solidconnection.mentor.domain.ChannelType.YOUTUBE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Channel;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.UniversitySelectType;
import com.example.solidconnection.mentor.dto.ChannelRequest;
import com.example.solidconnection.mentor.dto.ChannelResponse;
import com.example.solidconnection.mentor.dto.MentorMyPageCreateRequest;
import com.example.solidconnection.mentor.dto.MentorMyPageResponse;
import com.example.solidconnection.mentor.dto.MentorMyPageUpdateRequest;
import com.example.solidconnection.mentor.fixture.ChannelFixture;
import com.example.solidconnection.mentor.fixture.MentorApplicationFixture;
import com.example.solidconnection.mentor.fixture.MentorFixture;
import com.example.solidconnection.mentor.repository.ChannelRepositoryForTest;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.fixture.TermFixture;
import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.fixture.UniversityFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("멘토 마이페이지 서비스 테스트")
class MentorMyPageServiceTest {

    @Autowired
    private MentorMyPageService mentorMyPageService;

    @Autowired
    private MentorFixture mentorFixture;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private ChannelFixture channelFixture;

    @Autowired
    private TermFixture termFixture;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private UniversityFixture universityFixture;

    @Autowired
    private ChannelRepositoryForTest channelRepositoryForTest;

    @Autowired
    private MentorApplicationFixture mentorApplicationFixture;

    private SiteUser mentorUser;
    private Mentor mentor;
    private HostUniversity university;
    private SiteUser siteUser;
    private Term term;

    @BeforeEach
    void setUp() {
        term = termFixture.현재_학기("2025-1");
        university = universityFixture.메이지_대학();
        mentorUser = siteUserFixture.멘토(1, "멘토");
        mentor = mentorFixture.멘토(mentorUser.getId(), university.getId());
        siteUser = siteUserFixture.사용자();
    }

    @Nested
    class 멘토의_마이_페이지를_조회한다 {

        @Test
        void 성공적으로_조회한다() {
            // given
            Channel channel1 = channelFixture.채널(1, mentor);
            Channel channel2 = channelFixture.채널(2, mentor);

            // when
            MentorMyPageResponse response = mentorMyPageService.getMentorMyPage(mentorUser.getId());

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(mentor.getId()),
                    () -> assertThat(response.nickname()).isEqualTo(mentorUser.getNickname()),
                    () -> assertThat(response.universityName()).isEqualTo(university.getKoreanName()),
                    () -> assertThat(response.country()).isEqualTo(university.getCountry().getKoreanName()),
                    () -> assertThat(response.channels()).extracting(ChannelResponse::url)
                            .containsExactly(channel1.getUrl(), channel2.getUrl())
            );
        }
    }

    @Nested
    class 멘토의_마이_페이지를_수정한다 {

        @Test
        void 멘토_정보를_수정한다() {
            // given
            String newIntroduction = "새로운 자기소개";
            String newPassTip = "새로운 합격 팁";
            MentorMyPageUpdateRequest request = new MentorMyPageUpdateRequest(newIntroduction, newPassTip, List.of());

            // when
            mentorMyPageService.updateMentorMyPage(mentorUser.getId(), request);

            // then
            Mentor updatedMentor = mentorRepository.findById(mentor.getId()).get();
            assertAll(
                    () -> assertThat(updatedMentor.getIntroduction()).isEqualTo(newIntroduction),
                    () -> assertThat(updatedMentor.getPassTip()).isEqualTo(newPassTip)
            );
        }

        @Test
        void 기존보다_적게_채널_정보를_수정한다() {
            // given
            channelFixture.채널(1, mentor);
            channelFixture.채널(2, mentor);
            channelFixture.채널(3, mentor);
            channelFixture.채널(4, mentor);
            List<ChannelRequest> newChannels = List.of(new ChannelRequest(BLOG, "https://blog.com"));
            MentorMyPageUpdateRequest request = new MentorMyPageUpdateRequest("introduction", "passTip", newChannels);

            // when
            mentorMyPageService.updateMentorMyPage(mentorUser.getId(), request);

            // then
            List<Channel> updatedChannels = channelRepositoryForTest.findAllByMentorId(mentor.getId());
            assertThat(updatedChannels).extracting(Channel::getSequence, Channel::getType, Channel::getUrl)
                    .containsExactlyInAnyOrder(tuple(1, BLOG, "https://blog.com"));
        }

        @Test
        void 기존보다_많게_채널_정보를_수정한다() {
            // given
            channelFixture.채널(1, mentor);
            List<ChannelRequest> newChannels = List.of(
                    new ChannelRequest(BLOG, "https://blog.com"),
                    new ChannelRequest(INSTAGRAM, "https://instagram.com")
            );
            MentorMyPageUpdateRequest request = new MentorMyPageUpdateRequest("introduction", "passTip", newChannels);

            // when
            mentorMyPageService.updateMentorMyPage(mentorUser.getId(), request);

            // then
            List<Channel> updatedChannels = channelRepositoryForTest.findAllByMentorId(mentor.getId());
            assertThat(updatedChannels).extracting(Channel::getSequence, Channel::getType, Channel::getUrl)
                    .containsExactlyInAnyOrder(
                            tuple(1, BLOG, "https://blog.com"),
                            tuple(2, INSTAGRAM, "https://instagram.com")
                    );
        }
    }

    @Nested
    class 멘토의_마이페이지를_생성한다 {

        @Test
        void 멘토_정보를_생성한다() {
            // given
            String introduction = "멘토 자기소개";
            String passTip = "멘토의 합격 팁";
            List<ChannelRequest> channels = List.of(
                    new ChannelRequest(BLOG, "https://blog.com"),
                    new ChannelRequest(INSTAGRAM, "https://instagram.com"),
                    new ChannelRequest(YOUTUBE, "https://youtubr.com"),
                    new ChannelRequest(BRUNCH, "https://brunch.com")
            );
            MentorMyPageCreateRequest request = new MentorMyPageCreateRequest(introduction, passTip, channels);
            mentorApplicationFixture.승인된_멘토신청(siteUser.getId(), UniversitySelectType.CATALOG, university.getId());

            // when
            mentorMyPageService.createMentorMyPage(siteUser.getId(), request);

            // then
            Mentor createMentor = mentorRepository.findBySiteUserId(siteUser.getId()).get();
            List<Channel> createChannels = channelRepositoryForTest.findAllByMentorId(createMentor.getId());
            assertAll(
                    () -> assertThat(createMentor.getIntroduction()).isEqualTo(introduction),
                    () -> assertThat(createMentor.getPassTip()).isEqualTo(passTip),
                    () -> assertThat(createMentor.getTermId()).isEqualTo(term.getId()),
                    () -> assertThat(createMentor.getUniversityId()).isEqualTo(university.getId()),
                    () -> assertThat(createMentor.getSiteUserId()).isEqualTo(siteUser.getId()),
                    () -> assertThat(createMentor.getMenteeCount()).isEqualTo(0),
                    () -> assertThat(createMentor.isHasBadge()).isFalse(),
                    () -> assertThat(createChannels).extracting(Channel::getSequence, Channel::getType, Channel::getUrl)
                            .containsExactlyInAnyOrder(
                                    tuple(1, BLOG, "https://blog.com"),
                                    tuple(2, INSTAGRAM, "https://instagram.com"),
                                    tuple(3, YOUTUBE, "https://youtubr.com"),
                                    tuple(4, BRUNCH, "https://brunch.com")
                            )
            );
        }

        @Test
        void 이미_멘토_정보가_존재하는데_생성_요청_시_예외가_발생한다() {
            // given
            MentorMyPageCreateRequest request = new MentorMyPageCreateRequest("introduction", "passTip", List.of());
            mentorFixture.멘토(siteUser.getId(), university.getId());

            // when & then
            assertThatCode(() -> mentorMyPageService.createMentorMyPage(siteUser.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTOR_ALREADY_EXISTS.getMessage());
        }

        @Test
        void 채널을_제한_이상_생성하면_예외가_발생한다() {
            // given
            List<ChannelRequest> newChannels = List.of(
                    new ChannelRequest(BLOG, "https://blog.com"),
                    new ChannelRequest(INSTAGRAM, "https://instagram.com"),
                    new ChannelRequest(YOUTUBE, "https://youtubr.com"),
                    new ChannelRequest(BRUNCH, "https://brunch.com"),
                    new ChannelRequest(BLOG, "https://blog.com")
            );
            MentorMyPageCreateRequest request = new MentorMyPageCreateRequest("introduction", "passTip", newChannels);

            // when & then
            assertThatCode(() -> mentorMyPageService.createMentorMyPage(siteUser.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(CHANNEL_REGISTRATION_LIMIT_EXCEEDED.getMessage());
        }

        @Test
        void 멘토_승격_요청_없이_멘토_정보_생성_시_예외가_발생한다() {
            // given
            MentorMyPageCreateRequest request = new MentorMyPageCreateRequest("introduction", "passTip", List.of());

            // when & then
            assertThatCode(() -> mentorMyPageService.createMentorMyPage(siteUser.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTOR_APPLICATION_NOT_FOUND.getMessage());
        }

        @Test
        void 멘토_승격_요청_상태가_REJECTED_면_예외가_발생한다() {
            // given
            MentorMyPageCreateRequest request = new MentorMyPageCreateRequest("introduction", "passTip", List.of());
            mentorApplicationFixture.거절된_멘토신청(siteUser.getId(), UniversitySelectType.CATALOG, university.getId());

            // when & then
            assertThatCode(() -> mentorMyPageService.createMentorMyPage(siteUser.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTOR_APPLICATION_NOT_FOUND.getMessage());
        }

        @Test
        void 멘토_승격_요청_상태가_PENDING_면_예외가_발생한다() {
            // given
            MentorMyPageCreateRequest request = new MentorMyPageCreateRequest("introduction", "passTip", List.of());
            mentorApplicationFixture.대기중_멘토신청(siteUser.getId(), UniversitySelectType.CATALOG, university.getId());

            // when & then
            assertThatCode(() -> mentorMyPageService.createMentorMyPage(siteUser.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTOR_APPLICATION_NOT_FOUND.getMessage());
        }
    }
}
