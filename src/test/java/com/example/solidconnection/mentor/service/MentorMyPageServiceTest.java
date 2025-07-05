package com.example.solidconnection.mentor.service;

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
import com.example.solidconnection.siteuser.service.MyPageService;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static com.example.solidconnection.mentor.domain.ChannelType.BLOG;
import static com.example.solidconnection.mentor.domain.ChannelType.INSTAGRAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

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
    private ChannelRepositoryForTest channelRepositoryForTest;

    @MockBean
    private MyPageService myPageService;

    private SiteUser mentorUser;
    private Mentor mentor;
    private long universityId = 1L;

    @BeforeEach
    void setUp() {
        mentorUser = siteUserFixture.멘토(1, "멘토");
        mentor = mentorFixture.멘토(mentorUser.getId(), universityId);
    }

    @Nested
    class 멘토의_마이_페이지를_조회한다 {

        @Test
        void 성공적으로_조회한다() {
            // given
            Channel channel1 = channelFixture.채널(1, mentor);
            Channel channel2 = channelFixture.채널(2, mentor);

            // when
            MentorMyPageResponse response = mentorMyPageService.getMentorMyPage(mentorUser);

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(mentor.getId()),
                    () -> assertThat(response.nickname()).isEqualTo(mentorUser.getNickname()),
                    () -> assertThat(response.channels()).extracting(ChannelResponse::url)
                            .containsExactly(channel1.getUrl(), channel2.getUrl())
            );
        }
    }

    @Nested
    class 멘토의_마이_페이지를_수정한다 {

        @Test
        void 멘토의_사용자_정보_수정은_기존_수정로직에_위임한다() {
            // given
            String newNickname = "새로운 닉네임";
            MockMultipartFile newProfileImg = createImageFile();
            MentorMyPageUpdateRequest request = new MentorMyPageUpdateRequest(newNickname, "자기소개", "합격 팁", List.of());

            // when
            mentorMyPageService.updateMentorMyPage(mentorUser, request, newProfileImg);

            // then
            then(myPageService).should(times(1))
                    .updateMyPageInfo(mentorUser, newProfileImg, newNickname);
        }

        @Test
        void 멘토_정보를_수정한다() {
            // given
            String newIntroduction = "새로운 자기소개";
            String newPassTip = "새로운 합격 팁";
            MentorMyPageUpdateRequest request = new MentorMyPageUpdateRequest("nickname", newIntroduction, newPassTip, List.of());

            // when
            mentorMyPageService.updateMentorMyPage(mentorUser, request, null);

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
            MentorMyPageUpdateRequest request = new MentorMyPageUpdateRequest("nickname", "introduction", "passTip", newChannels);

            // when
            mentorMyPageService.updateMentorMyPage(mentorUser, request, null);
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

    private MockMultipartFile createImageFile() {
        return new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }
}
