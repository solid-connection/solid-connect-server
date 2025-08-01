package com.example.solidconnection.mentor.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.mentor.dto.MentoringForMenteeResponse;
import com.example.solidconnection.mentor.dto.MentoringForMentorResponse;
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

    private SiteUser mentorUser1, mentorUser2;
    private SiteUser menteeUser1, menteeUser2, menteeUser3;
    private Mentor mentor1, mentor2, mentor3;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        mentorUser1 = siteUserFixture.멘토(1, "mentor1");
        mentorUser2 = siteUserFixture.멘토(2, "mentor2");
        SiteUser mentorUser3 = siteUserFixture.멘토(3, "mentor3");
        menteeUser1 = siteUserFixture.사용자(1, "mentee1");
        menteeUser2 = siteUserFixture.사용자(2, "mentee2");
        menteeUser3 = siteUserFixture.사용자(3, "mentee3");
        mentor1 = mentorFixture.멘토(mentorUser1.getId(), 1L);
        mentor2 = mentorFixture.멘토(mentorUser2.getId(), 1L);
        mentor3 = mentorFixture.멘토(mentorUser3.getId(), 1L);
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

            // when
            SliceResponse<MentoringForMentorResponse> response = mentoringQueryService.getMentoringsForMentor(mentorUser1.getId(), pageable);

            // then
            assertThat(response.content()).extracting(MentoringForMentorResponse::mentoringId)
                    .containsExactlyInAnyOrder(
                            mentoring1.getId(),
                            mentoring2.getId(),
                            mentoring3.getId()
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
        void 승인된_멘토링_목록을_조회한다() {
            // given
            Mentoring mentoring1 = mentoringFixture.승인된_멘토링(mentor1.getId(), menteeUser1.getId());
            Mentoring mentoring2 = mentoringFixture.승인된_멘토링(mentor2.getId(), menteeUser1.getId());
            mentoringFixture.대기중_멘토링(mentor3.getId(), menteeUser1.getId());

            // when
            SliceResponse<MentoringForMenteeResponse> response = mentoringQueryService.getMentoringsForMentee(
                    menteeUser1.getId(), VerifyStatus.APPROVED, pageable);

            // then
            assertThat(response.content()).extracting(MentoringForMenteeResponse::mentoringId)
                    .containsExactlyInAnyOrder(
                            mentoring1.getId(),
                            mentoring2.getId()
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
        void 멘토링이_없는_경우_빈_리스트를_반환한다() {
            // when
            SliceResponse<MentoringForMenteeResponse> response = mentoringQueryService.getMentoringsForMentee(
                    mentorUser1.getId(), VerifyStatus.APPROVED, pageable);

            // then
            assertThat(response.content()).isEmpty();
        }
    }
}
