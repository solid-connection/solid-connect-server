package com.example.solidconnection.mentor.service;

import static com.example.solidconnection.mentor.domain.ChannelType.BLOG;
import static com.example.solidconnection.mentor.domain.ChannelType.INSTAGRAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.mentor.domain.Channel;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.dto.ChannelRequest;
import com.example.solidconnection.mentor.dto.ChannelResponse;
import com.example.solidconnection.mentor.dto.MentorMyPageResponse;
import com.example.solidconnection.mentor.dto.MentorMyPageUpdateRequest;
import com.example.solidconnection.mentor.fixture.ChannelFixture;
import com.example.solidconnection.mentor.fixture.MentorFixture;
import com.example.solidconnection.mentor.repository.ChannelRepositoryForTest;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.University;
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
    private MentorRepository mentorRepository;

    @Autowired
    private UniversityFixture universityFixture;

    @Autowired
    private ChannelRepositoryForTest channelRepositoryForTest;

    private SiteUser mentorUser;
    private Mentor mentor;
    private University university;

    @BeforeEach
    void setUp() {
        university = universityFixture.메이지_대학();
        mentorUser = siteUserFixture.멘토(1, "멘토");
        mentor = mentorFixture.멘토(mentorUser.getId(), university.getId());
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
        void 채널_정보를_수정한다() {
            // given
            List<ChannelRequest> newChannels = List.of(
                    new ChannelRequest(BLOG, "https://blog.com"),
                    new ChannelRequest(INSTAGRAM, "https://instagram.com")
            );
            MentorMyPageUpdateRequest request = new MentorMyPageUpdateRequest("introduction", "passTip", newChannels);

            // when
            mentorMyPageService.updateMentorMyPage(mentorUser.getId(), request);

            // then
            List<Channel> updatedChannels = channelRepositoryForTest.findAllByMentorId(mentor.getId());
            assertAll(
                    () -> assertThat(updatedChannels).extracting(Channel::getType)
                            .containsExactly(BLOG, INSTAGRAM),
                    () -> assertThat(updatedChannels).extracting(Channel::getUrl)
                            .containsExactly("https://blog.com", "https://instagram.com")
            );
        }
    }
}
