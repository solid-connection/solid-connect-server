package com.example.solidconnection.mentor.repository;

import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.fixture.MentorFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("멘토 배치 조회 레포지토리 테스트")
@TestContainerSpringBootTest
class MentorBatchQueryRepositoryTest {

    @Autowired
    private MentorBatchQueryRepository mentorBatchQueryRepository;

    @Autowired
    private MentorFixture mentorFixture;

    @Autowired
    private SiteUserFixture siteUserFixture;

    private long universityId = 1L; // todo: 멘토 인증 기능 추가 변경 필요
    private Mentor mentor1, mentor2;
    private SiteUser mentorUser1, mentorUser2, currentUser;

    @BeforeEach
    void setUp() {
        currentUser = siteUserFixture.사용자(1, "사용자");
        mentorUser1 = siteUserFixture.사용자(2, "멘토1");
        mentorUser2 = siteUserFixture.사용자(3, "멘토2");
        mentor1 = mentorFixture.멘토(mentorUser1.getId(), universityId);
        mentor2 = mentorFixture.멘토(mentorUser2.getId(), universityId);
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
    void 멘토_ID_와_현재_사용자의_지원_여부를_매핑한다() {
        // given
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
