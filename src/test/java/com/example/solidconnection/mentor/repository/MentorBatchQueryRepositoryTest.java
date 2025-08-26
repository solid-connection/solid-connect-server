package com.example.solidconnection.mentor.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.fixture.MentorFixture;
import com.example.solidconnection.mentor.fixture.MentoringFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.fixture.UniversityFixture;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("멘토 배치 조회 레포지토리 테스트")
@TestContainerSpringBootTest
class MentorBatchQueryRepositoryTest {

    @Autowired
    private MentorBatchQueryRepository mentorBatchQueryRepository;

    @Autowired
    private MentorFixture mentorFixture;

    @Autowired
    private MentoringFixture mentoringFixture;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private UniversityFixture universityFixture;

    private University university1, university2;
    private Mentor mentor1, mentor2;
    private SiteUser mentorUser1, mentorUser2, currentUser;

    @BeforeEach
    void setUp() {
        currentUser = siteUserFixture.사용자(1, "사용자");
        mentorUser1 = siteUserFixture.사용자(2, "멘토1");
        mentorUser2 = siteUserFixture.사용자(3, "멘토2");
        university1 = universityFixture.코펜하겐IT_대학();
        university2 = universityFixture.메모리얼_대학_세인트존스();
        mentor1 = mentorFixture.멘토(mentorUser1.getId(), university1.getId());
        mentor2 = mentorFixture.멘토(mentorUser2.getId(), university2.getId());
    }

    @Test
    void 멘토_ID_와_멘토_사용자를_매핑한다() {
        // given
        List<Mentor> mentors = List.of(mentor1, mentor2);

        // when
        Map<Long, SiteUser> mentorIdToSiteUser = mentorBatchQueryRepository.getMentorIdToSiteUserMap(mentors);

        // then
        assertAll(
                () -> assertThat(mentorIdToSiteUser.get(mentor1.getId()).getId()).isEqualTo(mentorUser1.getId()),
                () -> assertThat(mentorIdToSiteUser.get(mentor2.getId()).getId()).isEqualTo(mentorUser2.getId())
        );
    }

    @Test
    void 멘토_ID_와_멘토의_파견_대학교를_매핑한다() {
        // given
        List<Mentor> mentors = List.of(mentor1, mentor2);

        // when
        Map<Long, University> mentorIdToUniversity = mentorBatchQueryRepository.getMentorIdToUniversityMap(mentors);

        // then
        assertAll(
                () -> assertThat(mentorIdToUniversity.get(mentor1.getId()).getId()).isEqualTo(university1.getId()),
                () -> assertThat(mentorIdToUniversity.get(mentor2.getId()).getId()).isEqualTo(university2.getId())
        );
    }

    @Test
    void 멘토_ID_와_현재_사용자의_지원_여부를_매핑한다() {
        // given
        mentoringFixture.대기중_멘토링(mentor1.getId(), currentUser.getId());
        List<Mentor> mentors = List.of(mentor1, mentor2);

        // when
        Map<Long, Boolean> mentorIdToIsApplied = mentorBatchQueryRepository.getMentorIdToIsApplied(mentors, currentUser.getId());

        // then
        assertAll(
                () -> assertThat(mentorIdToIsApplied.get(mentor1.getId())).isTrue(),
                () -> assertThat(mentorIdToIsApplied.get(mentor2.getId())).isFalse()
        );
    }
}
