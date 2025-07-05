package com.example.solidconnection.mentor.service;

import com.example.solidconnection.mentor.domain.Channel;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.dto.ChannelResponse;
import com.example.solidconnection.mentor.dto.MentorMyPageResponse;
import com.example.solidconnection.mentor.fixture.ChannelFixture;
import com.example.solidconnection.mentor.fixture.MentorFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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

    private SiteUser mentorUser;
    private Mentor mentor;
    long universityId = 1L;

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
}
