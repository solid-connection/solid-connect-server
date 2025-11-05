package com.example.solidconnection.mentor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.mentor.dto.CheckMentoringRequest;
import com.example.solidconnection.mentor.dto.CheckedMentoringsResponse;
import com.example.solidconnection.mentor.dto.MentoringCountResponse;
import com.example.solidconnection.mentor.fixture.MentorFixture;
import com.example.solidconnection.mentor.fixture.MentoringFixture;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("멘토링 확인 서비스 테스트")
class MentoringCheckServiceTest {

    @Autowired
    private MentoringRepository mentoringRepository;

    @Autowired
    private MentoringCheckService mentoringCheckService;

    @Autowired
    private MentoringFixture mentoringFixture;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private MentorFixture mentorFixture;


    private SiteUser mentorUser1, mentorUser2;
    private SiteUser menteeUser1, menteeUser2, menteeUser3;
    private Mentor mentor1, mentor2, mentor3;

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
    }

    @Nested
    class 멘토의_멘토링_확인_테스트 {

        @Test
        void 멘토가_멘토링을_확인한다() {
            // given
            Mentoring mentoring1 = mentoringFixture.확인되지_않은_멘토링(mentor1.getId(), menteeUser1.getId());
            Mentoring mentoring2 = mentoringFixture.확인되지_않은_멘토링(mentor1.getId(), menteeUser2.getId());
            Mentoring mentoring3 = mentoringFixture.확인되지_않은_멘토링(mentor1.getId(), menteeUser3.getId());
            CheckMentoringRequest request = new CheckMentoringRequest(List.of(mentoring1.getId(), mentoring2.getId()));

            // when
            CheckedMentoringsResponse response = mentoringCheckService.checkMentoringsForMentor(mentorUser1.getId(), request);

            // then
            assertAll(
                    () -> assertThat(response.checkedMentoringIds()).containsExactlyInAnyOrder(mentoring1.getId(), mentoring2.getId()),
                    () -> assertThat(mentoringRepository.findById(mentoring1.getId()))
                            .hasValueSatisfying(mentoring -> assertThat(mentoring.getCheckedAtByMentor()).isNotNull()),
                    () -> assertThat(mentoringRepository.findById(mentoring2.getId()))
                            .hasValueSatisfying(mentoring -> assertThat(mentoring.getCheckedAtByMentor()).isNotNull()),
                    () -> assertThat(mentoringRepository.findById(mentoring3.getId()))
                            .hasValueSatisfying(mentoring -> assertThat(mentoring.getCheckedAtByMentor()).isNull())
            );
        }

        @Test
        void 다른_멘토의_멘토링은_확인하면_예외가_발생한다() {
            // given
            Mentoring mentoring2 = mentoringFixture.확인되지_않은_멘토링(mentor2.getId(), menteeUser2.getId());
            CheckMentoringRequest request = new CheckMentoringRequest(List.of(mentoring2.getId()));

            // when, then
            assertThatCode(() -> mentoringCheckService.checkMentoringsForMentor(mentorUser1.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.UNAUTHORIZED_MENTORING.getMessage());
        }
    }

    @Nested
    class 멘티의_멘토링_확인_테스트 {

        @Test
        void 멘티가_멘토링을_확인한다() {
            // given
            Mentoring mentoring1 = mentoringFixture.확인되지_않은_멘토링(mentor1.getId(), menteeUser1.getId());
            Mentoring mentoring2 = mentoringFixture.확인되지_않은_멘토링(mentor2.getId(), menteeUser1.getId());
            Mentoring mentoring3 = mentoringFixture.확인되지_않은_멘토링(mentor3.getId(), menteeUser1.getId());
            CheckMentoringRequest request = new CheckMentoringRequest(List.of(mentoring1.getId(), mentoring2.getId()));

            // when
            CheckedMentoringsResponse response = mentoringCheckService.checkMentoringsForMentee(menteeUser1.getId(), request);

            // then
            assertAll(
                    () -> assertThat(response.checkedMentoringIds()).containsExactlyInAnyOrder(mentoring1.getId(), mentoring2.getId()),
                    () -> assertThat(mentoringRepository.findById(mentoring1.getId()))
                            .hasValueSatisfying(mentoring -> assertThat(mentoring.getCheckedAtByMentee()).isNotNull()),
                    () -> assertThat(mentoringRepository.findById(mentoring2.getId()))
                            .hasValueSatisfying(mentoring -> assertThat(mentoring.getCheckedAtByMentee()).isNotNull()),
                    () -> assertThat(mentoringRepository.findById(mentoring3.getId()))
                            .hasValueSatisfying(mentoring -> assertThat(mentoring.getCheckedAtByMentee()).isNull())
            );
        }

        @Test
        void 다른_멘티의_멘토링을_확인하면_예외가_발생한다() {
            // given
            Mentoring mentoring2 = mentoringFixture.확인되지_않은_멘토링(mentor2.getId(), menteeUser2.getId());
            CheckMentoringRequest request = new CheckMentoringRequest(List.of(mentoring2.getId()));

            // when, then
            assertThatCode(() -> mentoringCheckService.checkMentoringsForMentee(menteeUser1.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.UNAUTHORIZED_MENTORING.getMessage());
        }

        @Nested
        class 멘토가_확인하지_않은_멘토링_개수_조회_테스트 {

            @Test
            void 멘토가_확인하지_않은_멘토링_개수를_반환한다() {
                // given
                mentoringFixture.확인되지_않은_멘토링(mentor1.getId(), menteeUser1.getId());
                mentoringFixture.확인되지_않은_멘토링(mentor1.getId(), menteeUser2.getId());
                mentoringFixture.승인된_멘토링(mentor1.getId(), menteeUser3.getId());

                // when
                MentoringCountResponse response = mentoringCheckService.getUncheckedMentoringCount(mentorUser1.getId());

                // then
                assertThat(response.uncheckedCount()).isEqualTo(2);
            }

            @Test
            void 멘토가_확인하지_않은_멘토링이_없으면_0을_반환한다() {
                // given
                mentoringFixture.승인된_멘토링(mentor1.getId(), menteeUser1.getId());

                // when
                MentoringCountResponse response = mentoringCheckService.getUncheckedMentoringCount(mentorUser1.getId());

                // then
                assertThat(response.uncheckedCount()).isZero();
            }
        }
    }
}