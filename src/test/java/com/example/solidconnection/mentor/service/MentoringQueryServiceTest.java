package com.example.solidconnection.mentor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.mentor.dto.MentoringCountResponse;
import com.example.solidconnection.mentor.dto.MentoringListResponse;
import com.example.solidconnection.mentor.dto.MentoringResponse;
import com.example.solidconnection.mentor.fixture.MentorFixture;
import com.example.solidconnection.mentor.fixture.MentoringFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

    private SiteUser mentorUser;
    private SiteUser menteeUser;
    private Mentor mentor;

    @BeforeEach
    void setUp() {
        mentorUser = siteUserFixture.멘토(1, "mentor1");
        menteeUser = siteUserFixture.사용자(2, "mentee1");
        mentor = mentorFixture.멘토(mentorUser.getId(), 1L);
    }

    @Nested
    class 멘토링_목록_조회_테스트 {

        @Test
        void 멘토의_모든_멘토링을_조회한다() {
            // given
            Mentoring mentoring1 = mentoringFixture.대기중_멘토링(mentor.getId(), menteeUser.getId());
            Mentoring mentoring2 = mentoringFixture.승인된_멘토링(mentor.getId(), menteeUser.getId());
            Mentoring mentoring3 = mentoringFixture.거절된_멘토링(mentor.getId(), menteeUser.getId(), "거절 사유");

            // when
            MentoringListResponse responses = mentoringQueryService.getMentorings(mentorUser.getId());

            // then
            assertAll(
                    () -> assertThat(responses.requests()).hasSize(3),
                    () -> assertThat(responses.requests()).extracting(MentoringResponse::mentoringId)
                            .containsExactlyInAnyOrder(
                                    mentoring1.getId(),
                                    mentoring2.getId(),
                                    mentoring3.getId()
                            )
            );
        }

        @Test
        void 멘토링이_없는_경우_빈_리스트를_반환한다() {
            // when
            MentoringListResponse responses = mentoringQueryService.getMentorings(mentorUser.getId());

            // then
            assertThat(responses.requests()).isEmpty();
        }
    }

    @Nested
    class 새_멘토링_개수_조회_테스트 {

        @Test
        void 확인되지_않은_멘토링_개수를_반환한다() {
            // given
            mentoringFixture.확인되지_않은_멘토링(mentor.getId(), menteeUser.getId());
            mentoringFixture.확인되지_않은_멘토링(mentor.getId(), menteeUser.getId());
            mentoringFixture.승인된_멘토링(mentor.getId(), menteeUser.getId());

            // when
            MentoringCountResponse response = mentoringQueryService.getNewMentoringsCount(mentorUser.getId());

            // then
            assertThat(response.uncheckedCount()).isEqualTo(2);
        }

        @Test
        void 확인되지_않은_멘토링이_없으면_0을_반환한다() {
            // given
            mentoringFixture.승인된_멘토링(mentor.getId(), menteeUser.getId());

            // when
            MentoringCountResponse response = mentoringQueryService.getNewMentoringsCount(mentorUser.getId());

            // then
            assertThat(response.uncheckedCount()).isZero();
        }
    }
}
