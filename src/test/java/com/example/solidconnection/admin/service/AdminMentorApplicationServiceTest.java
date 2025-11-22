package com.example.solidconnection.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.solidconnection.admin.dto.MentorApplicationSearchCondition;
import com.example.solidconnection.admin.dto.MentorApplicationSearchResponse;
import com.example.solidconnection.mentor.domain.MentorApplication;
import com.example.solidconnection.mentor.domain.MentorApplicationStatus;
import com.example.solidconnection.mentor.domain.UniversitySelectType;
import com.example.solidconnection.mentor.fixture.MentorApplicationFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.fixture.UniversityFixture;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@TestContainerSpringBootTest
@DisplayName("어학 검증 관리자 서비스 테스트")
class AdminMentorApplicationServiceTest {

    @Autowired
    private AdminMentorApplicationService adminMentorApplicationService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private MentorApplicationFixture mentorApplicationFixture;

    @Autowired
    private UniversityFixture universityFixture;

    private MentorApplication mentorApplication1;
    private MentorApplication mentorApplication2;
    private MentorApplication mentorApplication3;
    private MentorApplication mentorApplication4;
    private MentorApplication mentorApplication5;
    private MentorApplication mentorApplication6;

    @BeforeEach
    void setUp() {
        SiteUser user1 = siteUserFixture.사용자(1, "test1");
        SiteUser user2 = siteUserFixture.사용자(2, "test2");
        SiteUser user3 = siteUserFixture.사용자(3, "test3");
        SiteUser user4 = siteUserFixture.사용자(4, "test4");
        SiteUser user5 = siteUserFixture.사용자(5, "test5");
        SiteUser user6 = siteUserFixture.사용자(6, "test6");
        University university1 = universityFixture.메이지_대학();
        University university2 = universityFixture.괌_대학();
        University university3 = universityFixture.그라츠_대학();
        mentorApplication1 = mentorApplicationFixture.승인된_멘토신청(user1.getId(), UniversitySelectType.CATALOG, university1.getId());
        mentorApplication2 = mentorApplicationFixture.대기중_멘토신청(user2.getId(), UniversitySelectType.CATALOG, university2.getId());
        mentorApplication3 = mentorApplicationFixture.거절된_멘토신청(user3.getId(), UniversitySelectType.CATALOG, university3.getId());
        mentorApplication4 = mentorApplicationFixture.승인된_멘토신청(user4.getId(), UniversitySelectType.CATALOG, university3.getId());
        mentorApplication5 = mentorApplicationFixture.대기중_멘토신청(user5.getId(), UniversitySelectType.CATALOG, university1.getId());
        mentorApplication6 = mentorApplicationFixture.거절된_멘토신청(user6.getId(), UniversitySelectType.CATALOG, university2.getId());
    }

    @Nested
    class 멘토_승격_지원서_목록_조회 {

        @Test
        void 멘토_승격_상태를_조건으로_페이징하여_조회한다() {
            // given
            MentorApplicationSearchCondition condition = new MentorApplicationSearchCondition(MentorApplicationStatus.PENDING,null, null);
            Pageable pageable = PageRequest.of(0, 10);
            List<MentorApplication> expectedMentorApplications = List.of(mentorApplication2, mentorApplication5);

            // when
            Page<MentorApplicationSearchResponse> response = adminMentorApplicationService.searchMentorApplications(condition, pageable);

            // then
            assertThat(response.get()).hasSize(expectedMentorApplications.size());
            assertThat(response.getContent())
                    .extracting(content -> content.mentorApplicationResponse().mentorApplicationStatus())
                    .containsOnly(MentorApplicationStatus.PENDING);
        }

        @Test
        void 닉네임_keyword_에_맞는_멘토_지원서를_페이징하여_조회한다(){
            // given
            String nickname = "test1";
            MentorApplicationSearchCondition condition = new MentorApplicationSearchCondition(null, nickname, null);
            Pageable pageable = PageRequest.of(0, 10);
            List<MentorApplication> expectedMentorApplications = List.of(mentorApplication1);

            // when
            Page<MentorApplicationSearchResponse> response = adminMentorApplicationService.searchMentorApplications(condition, pageable);

            // then
            assertThat(response.get()).hasSize(expectedMentorApplications.size());
            assertThat(response.getContent())
                    .extracting(content -> content.siteUserResponse().nickname())
                    .containsOnly(nickname);
        }

        @Test
        void 대학명_keyword_에_맞는_멘토_지원서를_페이징하여_조회한다(){
            // given
            String universityKoreanName = "메이지 대학";
            MentorApplicationSearchCondition condition = new MentorApplicationSearchCondition(null, universityKoreanName, null);
            Pageable pageable = PageRequest.of(0, 10);
            List<MentorApplication> expectedMentorApplications = List.of(mentorApplication1, mentorApplication5);

            // when
            Page<MentorApplicationSearchResponse> response = adminMentorApplicationService.searchMentorApplications(condition, pageable);

            // then
            assertThat(response.get()).hasSize(expectedMentorApplications.size());
            assertThat(response.getContent())
                    .extracting(content -> content.mentorApplicationResponse().university())
                    .containsOnly(universityKoreanName);
        }

        @Test
        void 지역명_keyword_에_맞는_멘토_지원서를_페이징하여_조회한다(){
            // given
            String regionKoreanName = "유럽";
            MentorApplicationSearchCondition condition = new MentorApplicationSearchCondition(null, regionKoreanName, null);
            Pageable pageable = PageRequest.of(0, 10);
            List<MentorApplication> expectedMentorApplications = List.of(mentorApplication3, mentorApplication4);

            // when
            Page<MentorApplicationSearchResponse> response = adminMentorApplicationService.searchMentorApplications(condition, pageable);

            // then
            assertThat(response.get()).hasSize(expectedMentorApplications.size());
            assertThat(response.getContent())
                    .extracting(content -> content.mentorApplicationResponse().region())
                    .containsOnly(regionKoreanName);
        }

        @Test
        void 나라명_keyword_에_맞는_멘토_지원서를_페이징하여_조회한다(){
            // given
            String countryKoreanName = "오스트리아";
            MentorApplicationSearchCondition condition = new MentorApplicationSearchCondition(null, countryKoreanName, null);
            Pageable pageable = PageRequest.of(0, 10);
            List<MentorApplication> expectedMentorApplications = List.of(mentorApplication2, mentorApplication6);

            // when
            Page<MentorApplicationSearchResponse> response = adminMentorApplicationService.searchMentorApplications(condition, pageable);

            // then
            assertThat(response.get()).hasSize(expectedMentorApplications.size());
            assertThat(response.getContent())
                    .extracting(content -> content.mentorApplicationResponse().country())
                    .containsOnly(countryKoreanName);
        }

        @Test
        void 모든_조건으로_페이징하여_조회한다() {
            // given
            String regionKoreanName = "영미권";
            MentorApplicationSearchCondition condition = new MentorApplicationSearchCondition(MentorApplicationStatus.PENDING, regionKoreanName, LocalDate.now());
            Pageable pageable = PageRequest.of(0, 10);
            List<MentorApplication> expectedMentorApplications = List.of(mentorApplication2);

            // when
            Page<MentorApplicationSearchResponse> response = adminMentorApplicationService.searchMentorApplications(condition, pageable);

            // then
            assertThat(response.get()).hasSize(expectedMentorApplications.size());
            assertThat(response.getContent())
                    .extracting(content -> content.mentorApplicationResponse().mentorApplicationStatus())
                    .containsOnly(MentorApplicationStatus.PENDING);
            assertThat(response.getContent())
                    .extracting(content -> content.mentorApplicationResponse().region())
                    .containsOnly(regionKoreanName);
        }
    }
}
