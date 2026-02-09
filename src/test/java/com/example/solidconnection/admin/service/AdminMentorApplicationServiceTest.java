package com.example.solidconnection.admin.service;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_APPLICATION_ALREADY_CONFIRMED;
import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_APPLICATION_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_APPLICATION_NOT_OTHER_STATUS;
import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_APPLICATION_UNIVERSITY_NOT_SELECTED;
import static com.example.solidconnection.common.exception.ErrorCode.UNIVERSITY_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.admin.dto.MentorApplicationCountResponse;
import com.example.solidconnection.admin.dto.MentorApplicationHistoryResponse;
import com.example.solidconnection.admin.dto.MentorApplicationRejectRequest;
import com.example.solidconnection.admin.dto.MentorApplicationSearchCondition;
import com.example.solidconnection.admin.dto.MentorApplicationSearchResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.MentorApplication;
import com.example.solidconnection.mentor.domain.MentorApplicationStatus;
import com.example.solidconnection.mentor.domain.UniversitySelectType;
import com.example.solidconnection.mentor.fixture.MentorApplicationFixture;
import com.example.solidconnection.mentor.repository.MentorApplicationRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.university.domain.HostUniversity;
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
@DisplayName("멘토 지원서 관리자 서비스 테스트")
class AdminMentorApplicationServiceTest {

    @Autowired
    private AdminMentorApplicationService adminMentorApplicationService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private MentorApplicationFixture mentorApplicationFixture;

    @Autowired
    private UniversityFixture universityFixture;

    @Autowired
    private MentorApplicationRepository mentorApplicationRepository;

    private MentorApplication mentorApplication1;
    private MentorApplication mentorApplication2;
    private MentorApplication mentorApplication3;
    private MentorApplication mentorApplication4;
    private MentorApplication mentorApplication5;
    private MentorApplication mentorApplication6;
    private MentorApplication mentorApplication7;
    private MentorApplication mentorApplication8;

    private SiteUser user;
    private HostUniversity university;

    @BeforeEach
    void setUp() {
        SiteUser user1 = siteUserFixture.사용자(1, "test1");
        SiteUser user2 = siteUserFixture.사용자(2, "test2");
        SiteUser user3 = siteUserFixture.사용자(3, "test3");
        SiteUser user4 = siteUserFixture.사용자(4, "test4");
        SiteUser user5 = siteUserFixture.사용자(5, "test5");
        SiteUser user6 = siteUserFixture.사용자(6, "test6");
        SiteUser user7 = siteUserFixture.사용자(7, "test7");
        SiteUser user8 = siteUserFixture.사용자(8, "test8");
        HostUniversity university1 = universityFixture.메이지_대학();
        HostUniversity university2 = universityFixture.괌_대학();
        HostUniversity university3 = universityFixture.그라츠_대학();
        mentorApplication1 = mentorApplicationFixture.승인된_멘토신청(user1.getId(), UniversitySelectType.CATALOG, university1.getId());
        mentorApplication2 = mentorApplicationFixture.대기중_멘토신청(user2.getId(), UniversitySelectType.CATALOG, university2.getId());
        mentorApplication3 = mentorApplicationFixture.거절된_멘토신청(user3.getId(), UniversitySelectType.CATALOG, university3.getId());
        mentorApplication4 = mentorApplicationFixture.승인된_멘토신청(user4.getId(), UniversitySelectType.CATALOG, university3.getId());
        mentorApplication5 = mentorApplicationFixture.대기중_멘토신청(user5.getId(), UniversitySelectType.CATALOG, university1.getId());
        mentorApplication6 = mentorApplicationFixture.거절된_멘토신청(user6.getId(), UniversitySelectType.CATALOG, university2.getId());
        mentorApplication7 = mentorApplicationFixture.대기중_멘토신청(user7.getId(), UniversitySelectType.OTHER, null);
        mentorApplication8 = mentorApplicationFixture.거절된_멘토신청(user8.getId(), UniversitySelectType.OTHER, null);

        user = siteUserFixture.사용자(9, "test9");
        university = universityFixture.네바다주립_대학_라스베이거스();
    }

    @Nested
    class 멘토_승격_지원서_목록_조회 {

        @Test
        void 멘토_승격_상태를_조건으로_페이징하여_조회한다() {
            // given
            MentorApplicationSearchCondition condition = new MentorApplicationSearchCondition(MentorApplicationStatus.PENDING,null, null, null);
            Pageable pageable = PageRequest.of(0, 10);
            List<MentorApplication> expectedMentorApplications = List.of(mentorApplication2, mentorApplication5, mentorApplication7);

            // when
            Page<MentorApplicationSearchResponse> response = adminMentorApplicationService.searchMentorApplications(condition, pageable);

            // then
            assertAll(
                    () -> assertThat(response.getContent()).hasSize(expectedMentorApplications.size()),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.mentorApplicationResponse().id())
                            .containsOnly(expectedMentorApplications.stream()
                                                  .map(MentorApplication::getId)
                                                  .toArray(Long[]::new)),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.mentorApplicationResponse().mentorApplicationStatus())
                            .containsOnly(MentorApplicationStatus.PENDING)
            );
        }

        @Test
        void 닉네임_keyword_에_맞는_멘토_지원서를_페이징하여_조회한다(){
            // given
            String nickname = "test1";
            MentorApplicationSearchCondition condition = new MentorApplicationSearchCondition(null, nickname, null, null);
            Pageable pageable = PageRequest.of(0, 10);
            List<MentorApplication> expectedMentorApplications = List.of(mentorApplication1);

            // when
            Page<MentorApplicationSearchResponse> response = adminMentorApplicationService.searchMentorApplications(condition, pageable);

            // then
            assertAll(
                    () -> assertThat(response.getContent()).hasSize(expectedMentorApplications.size()),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.mentorApplicationResponse().id())
                            .containsOnly(expectedMentorApplications.stream()
                                                  .map(MentorApplication::getId)
                                                  .toArray(Long[]::new)),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.siteUserResponse().nickname())
                            .containsOnly(nickname)
            );
        }

        @Test
        void 대학명_keyword_에_맞는_멘토_지원서를_페이징하여_조회한다(){
            // given
            String universityKoreanName = "메이지 대학";
            MentorApplicationSearchCondition condition = new MentorApplicationSearchCondition(null, universityKoreanName, null, null);
            Pageable pageable = PageRequest.of(0, 10);
            List<MentorApplication> expectedMentorApplications = List.of(mentorApplication1, mentorApplication5);

            // when
            Page<MentorApplicationSearchResponse> response = adminMentorApplicationService.searchMentorApplications(condition, pageable);

            // then
            assertAll(
                    () -> assertThat(response.getContent()).hasSize(expectedMentorApplications.size()),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.mentorApplicationResponse().id())
                            .containsOnly(expectedMentorApplications.stream()
                                                  .map(MentorApplication::getId)
                                                  .toArray(Long[]::new)),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.mentorApplicationResponse().university())
                            .containsOnly(universityKoreanName)
            );
        }

        @Test
        void 지역명_keyword_에_맞는_멘토_지원서를_페이징하여_조회한다(){
            // given
            String regionKoreanName = "유럽";
            MentorApplicationSearchCondition condition = new MentorApplicationSearchCondition(null, regionKoreanName, null, null);
            Pageable pageable = PageRequest.of(0, 10);
            List<MentorApplication> expectedMentorApplications = List.of(mentorApplication3, mentorApplication4);

            // when
            Page<MentorApplicationSearchResponse> response = adminMentorApplicationService.searchMentorApplications(condition, pageable);

            // then
            assertAll(
                    () -> assertThat(response.getContent()).hasSize(expectedMentorApplications.size()),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.mentorApplicationResponse().id())
                            .containsOnly(expectedMentorApplications.stream()
                                                  .map(MentorApplication::getId)
                                                  .toArray(Long[]::new)),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.mentorApplicationResponse().region())
                            .containsOnly(regionKoreanName)
            );
        }

        @Test
        void 나라명_keyword_에_맞는_멘토_지원서를_페이징하여_조회한다(){
            // given
            String countryKoreanName = "오스트리아";
            MentorApplicationSearchCondition condition = new MentorApplicationSearchCondition(null, countryKoreanName, null,null);
            Pageable pageable = PageRequest.of(0, 10);
            List<MentorApplication> expectedMentorApplications = List.of(mentorApplication3, mentorApplication4);

            // when
            Page<MentorApplicationSearchResponse> response = adminMentorApplicationService.searchMentorApplications(condition, pageable);

            // then
            assertAll(
                    () -> assertThat(response.getContent()).hasSize(expectedMentorApplications.size()),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.mentorApplicationResponse().id())
                            .containsOnly(expectedMentorApplications.stream()
                                                  .map(MentorApplication::getId)
                                                  .toArray(Long[]::new)),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.mentorApplicationResponse().country())
                            .containsOnly(countryKoreanName)
            );
        }

        @Test
        void CATALOG_타입의_멘토_지원서만_조회한다() {
            // given
            MentorApplicationSearchCondition condition = new MentorApplicationSearchCondition(null, null, null, UniversitySelectType.CATALOG);
            Pageable pageable = PageRequest.of(0, 10);
            List<MentorApplication> expectedMentorApplications = List.of(
                    mentorApplication1, mentorApplication2, mentorApplication3,
                    mentorApplication4, mentorApplication5, mentorApplication6
            );

            // when
            Page<MentorApplicationSearchResponse> response = adminMentorApplicationService.searchMentorApplications(condition, pageable);

            // then
            assertAll(
                    () -> assertThat(response.getContent()).hasSize(expectedMentorApplications.size()),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.mentorApplicationResponse().id())
                            .containsOnly(expectedMentorApplications.stream()
                                                  .map(MentorApplication::getId)
                                                  .toArray(Long[]::new)),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.mentorApplicationResponse().universitySelectType())
                            .containsOnly(UniversitySelectType.CATALOG)
            );
        }

        @Test
        void OTHER_타입의_멘토_지원서만_조회한다() {
            // given
            MentorApplicationSearchCondition condition = new MentorApplicationSearchCondition(null, null, null, UniversitySelectType.OTHER);
            Pageable pageable = PageRequest.of(0, 10);
            List<MentorApplication> expectedMentorApplications = List.of(mentorApplication7, mentorApplication8);

            // when
            Page<MentorApplicationSearchResponse> response = adminMentorApplicationService.searchMentorApplications(condition, pageable);

            // then
            assertAll(
                    () -> assertThat(response.getContent()).hasSize(expectedMentorApplications.size()),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.mentorApplicationResponse().id())
                            .containsOnly(expectedMentorApplications.stream()
                                                  .map(MentorApplication::getId)
                                                  .toArray(Long[]::new)),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.mentorApplicationResponse().universitySelectType())
                            .containsOnly(UniversitySelectType.OTHER)
            );
        }

        @Test
        void 모든_조건으로_페이징하여_조회한다() {
            // given
            String regionKoreanName = "영미권";
            MentorApplicationSearchCondition condition = new MentorApplicationSearchCondition(MentorApplicationStatus.PENDING, regionKoreanName, LocalDate.now(), UniversitySelectType.CATALOG);
            Pageable pageable = PageRequest.of(0, 10);
            List<MentorApplication> expectedMentorApplications = List.of(mentorApplication2);

            // when
            Page<MentorApplicationSearchResponse> response = adminMentorApplicationService.searchMentorApplications(condition, pageable);

            // then
            assertAll(
                    () -> assertThat(response.getContent()).hasSize(expectedMentorApplications.size()),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.mentorApplicationResponse().id())
                            .containsOnly(expectedMentorApplications.stream()
                                                  .map(MentorApplication::getId)
                                                  .toArray(Long[]::new)),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.mentorApplicationResponse().mentorApplicationStatus())
                            .containsOnly(MentorApplicationStatus.PENDING),
                    () -> assertThat(response.getContent())
                            .extracting(content -> content.mentorApplicationResponse().region())
                            .containsOnly(regionKoreanName)
            );
        }
    }

    @Nested
    class 멘토_승격_지원서_승인{

        @Test
        void 대기중인_멘토_지원서를_승인한다() {
            // given
            long pendingMentorApplicationId = mentorApplication2.getId();

            // when
            adminMentorApplicationService.approveMentorApplication(pendingMentorApplicationId);

            // then
            MentorApplication result = mentorApplicationRepository.findById(mentorApplication2.getId()).get();
            assertAll(
                    () -> assertThat(result.getMentorApplicationStatus()).isEqualTo(MentorApplicationStatus.APPROVED),
                    () -> assertThat(result.getApprovedAt()).isNotNull()
            );
        }

        @Test
        void 대학이_선택되지_않은_멘토_지원서를_승인하면_예외가_발생한다(){
            // given
            SiteUser user = siteUserFixture.사용자();
            MentorApplication noUniversityIdMentorApplication = mentorApplicationFixture.대기중_멘토신청(user.getId(), UniversitySelectType.OTHER, null);

            // when & then
            assertThatCode(() -> adminMentorApplicationService.approveMentorApplication(noUniversityIdMentorApplication.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTOR_APPLICATION_UNIVERSITY_NOT_SELECTED.getMessage());
        }

        @Test
        void 이미_승인된_멘토_지원서를_승인하면_예외가_발생한다() {
            // given
            long approvedMentorApplicationId = mentorApplication1.getId();

            // when & then
            assertThatCode(() -> adminMentorApplicationService.approveMentorApplication(approvedMentorApplicationId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTOR_APPLICATION_ALREADY_CONFIRMED.getMessage());
        }

        @Test
        void 이미_거절된_멘토_지원서를_승인하면_예외가_발생한다() {
            // given
            long rejectedMentorApplicationId = mentorApplication3.getId();

            // when & then
            assertThatCode(() -> adminMentorApplicationService.approveMentorApplication(rejectedMentorApplicationId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTOR_APPLICATION_ALREADY_CONFIRMED.getMessage());
        }

        @Test
        void 존재하지_않는_멘토_지원서를_승인하면_예외가_발생한다() {
            // given
            long nonExistentId = 99999L;

            // when & then
            assertThatCode(() -> adminMentorApplicationService.approveMentorApplication(nonExistentId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTOR_APPLICATION_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 멘토_승격_지원서_거절{

        @Test
        void 대기중인_멘토_지원서를_거절한다() {
            // given
            long pendingMentorApplicationId = mentorApplication2.getId();
            MentorApplicationRejectRequest request = new MentorApplicationRejectRequest("파견학교 인증 자료 누락");

            // when
            adminMentorApplicationService.rejectMentorApplication(pendingMentorApplicationId, request);

            // then
            MentorApplication result = mentorApplicationRepository.findById(mentorApplication2.getId()).get();
            assertAll(
                    () -> assertThat(result.getMentorApplicationStatus()).isEqualTo(MentorApplicationStatus.REJECTED),
                    () -> assertThat(result.getRejectedReason()).isEqualTo(request.rejectedReason())
            );
        }

        @Test
        void 이미_승인된_멘토_지원서를_거절하면_예외가_발생한다() {
            // given
            long approvedMentorApplicationId = mentorApplication1.getId();
            MentorApplicationRejectRequest request = new MentorApplicationRejectRequest("파견학교 인증 자료 누락");

            // when & then
            assertThatCode(() -> adminMentorApplicationService.rejectMentorApplication(approvedMentorApplicationId, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTOR_APPLICATION_ALREADY_CONFIRMED.getMessage());
        }

        @Test
        void 이미_거절된_멘토_지원서를_거절하면_예외가_발생한다() {
            // given
            long rejectedMentorApplicationId = mentorApplication3.getId();
            MentorApplicationRejectRequest request = new MentorApplicationRejectRequest("파견학교 인증 자료 누락");

            // when & then
            assertThatCode(() -> adminMentorApplicationService.rejectMentorApplication(rejectedMentorApplicationId, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTOR_APPLICATION_ALREADY_CONFIRMED.getMessage());
        }

        @Test
        void 존재하지_않는_멘토_지원서를_거절하면_예외가_발생한다() {
            // given
            long nonExistentId = 99999L;
            MentorApplicationRejectRequest request = new MentorApplicationRejectRequest("파견학교 인증 자료 누락");

            // when & then
            assertThatCode(() -> adminMentorApplicationService.rejectMentorApplication(nonExistentId, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTOR_APPLICATION_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 멘토_지원서_상태별_개수_조회 {

        @Test
        void 상태별_멘토_지원서_개수를_조회한다() {
            // given
            List<MentorApplication> expectedApprovedCount = List.of(mentorApplication1, mentorApplication4);
            List<MentorApplication> expectedPendingCount = List.of(mentorApplication2, mentorApplication5, mentorApplication7);
            List<MentorApplication> expectedRejectedCount = List.of(mentorApplication3, mentorApplication6, mentorApplication8);

            // when
            MentorApplicationCountResponse response = adminMentorApplicationService.getMentorApplicationCount();

            // then
            assertAll(
                    () -> assertThat(response.approvedCount()).isEqualTo(expectedApprovedCount.size()),
                    () -> assertThat(response.pendingCount()).isEqualTo(expectedPendingCount.size()),
                    () -> assertThat(response.rejectedCount()).isEqualTo(expectedRejectedCount.size())
            );
        }

        @Test
        void 멘토_지원서가_없으면_모든_개수가_0이다() {
            // given
            mentorApplicationRepository.deleteAll();

            // when
            MentorApplicationCountResponse response = adminMentorApplicationService.getMentorApplicationCount();

            // then
            assertAll(
                    () -> assertThat(response.approvedCount()).isEqualTo(0L),
                    () -> assertThat(response.pendingCount()).isEqualTo(0L),
                    () -> assertThat(response.rejectedCount()).isEqualTo(0L)
            );
        }
    }

    @Nested
    class 멘토_지원서에_대학_매핑 {

        @Test
        void OTHER_타입의_멘토_지원서에_대학을_매핑하면_대학이_할당되고_타입이_CATALOG로_변경된다() {
            // given
            long otherTypeMentorApplicationId = mentorApplication7.getId();
            HostUniversity university = universityFixture.아칸소_주립_대학();

            // when
            adminMentorApplicationService.assignUniversity(otherTypeMentorApplicationId, university.getId());

            // then
            MentorApplication result = mentorApplicationRepository.findById(otherTypeMentorApplicationId).get();
            assertAll(
                    () -> assertThat(result.getUniversityId()).isEqualTo(university.getId()),
                    () -> assertThat(result.getUniversitySelectType()).isEqualTo(UniversitySelectType.CATALOG)
            );
        }

        @Test
        void 존재하지_않는_멘토_지원서에_대학을_매핑하면_예외_응답을_반환한다() {
            // given
            long nonExistentId = 99999L;
            HostUniversity university = universityFixture.메모리얼_대학_세인트존스();

            // when & then
            assertThatCode(() -> adminMentorApplicationService.assignUniversity(nonExistentId, university.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTOR_APPLICATION_NOT_FOUND.getMessage());
        }

        @Test
        void CATALOG_타입의_멘토_지원서에_대학을_매핑하면_예외_응답을_반환한다() {
            // given
            long catalogTypeMentorApplicationId = mentorApplication2.getId();
            HostUniversity university = universityFixture.서던덴마크_대학();

            // when & then
            assertThatCode(() -> adminMentorApplicationService.assignUniversity(catalogTypeMentorApplicationId, university.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MENTOR_APPLICATION_NOT_OTHER_STATUS.getMessage());
        }

        @Test
        void 존재하지_않는_대학을_매핑하면_예외_응답을_반환한다() {
            // given
            long otherTypeMentorApplicationId = mentorApplication7.getId();
            long nonExistentUniversityId = 99999L;

            // when & then
            assertThatCode(() -> adminMentorApplicationService.assignUniversity(otherTypeMentorApplicationId, nonExistentUniversityId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UNIVERSITY_NOT_FOUND.getMessage());
        }
    }
    
    @Nested
    class 멘토_지원서_이력_조회 {

        @Test
        void 사용자의_멘토_지원서_이력을_최신_생성_내림차순으로_조회한다() {
            // given
            MentorApplication app1 = mentorApplicationFixture.거절된_멘토신청(user.getId(), UniversitySelectType.CATALOG, university.getId());
            MentorApplication app2 = mentorApplicationFixture.거절된_멘토신청(user.getId(), UniversitySelectType.CATALOG, university.getId());
            MentorApplication app3 = mentorApplicationFixture.승인된_멘토신청(user.getId(), UniversitySelectType.CATALOG, university.getId());

            // when
            List<MentorApplicationHistoryResponse> response = adminMentorApplicationService.findMentorApplicationHistory(user.getId());

            // then
            assertAll(
                    () -> assertThat(response).hasSize(3),
                    () -> assertThat(response)
                            .extracting(MentorApplicationHistoryResponse::id)
                            .containsExactly(app3.getId(), app2.getId(), app1.getId()),
                    () -> assertThat(response)
                            .extracting(MentorApplicationHistoryResponse::applicationOrder)
                            .containsExactly(3,2,1)
            );
        }

        @Test
        void 지원서가_5개를_초과하면_최신_5개만_최신_생성_내림차순으로_조회한다() {
            // given
            MentorApplication app1 = mentorApplicationFixture.거절된_멘토신청(user.getId(), UniversitySelectType.CATALOG, university.getId());
            MentorApplication app2 = mentorApplicationFixture.거절된_멘토신청(user.getId(), UniversitySelectType.CATALOG, university.getId());
            MentorApplication app3 = mentorApplicationFixture.거절된_멘토신청(user.getId(), UniversitySelectType.CATALOG, university.getId());
            MentorApplication app4 = mentorApplicationFixture.거절된_멘토신청(user.getId(), UniversitySelectType.CATALOG, university.getId());
            MentorApplication app5 = mentorApplicationFixture.거절된_멘토신청(user.getId(), UniversitySelectType.CATALOG, university.getId());
            MentorApplication app6 = mentorApplicationFixture.거절된_멘토신청(user.getId(), UniversitySelectType.CATALOG, university.getId());
            MentorApplication app7 = mentorApplicationFixture.승인된_멘토신청(user.getId(), UniversitySelectType.CATALOG, university.getId());

            // when
            List<MentorApplicationHistoryResponse> response = adminMentorApplicationService.findMentorApplicationHistory(user.getId());

            // then
            assertAll(
                    () -> assertThat(response).hasSize(5),
                    () -> assertThat(response)
                            .extracting(MentorApplicationHistoryResponse::id)
                            .containsExactly(app7.getId(), app6.getId(), app5.getId(), app4.getId(), app3.getId()),
                    () -> assertThat(response)
                            .extracting(MentorApplicationHistoryResponse::applicationOrder)
                            .containsExactly(7,6,5,4,3)
            );
        }

        @Test
        void 지원서_이력이_없으면_빈_목록을_반환한다() {
            // given
            long withoutApplicationUserId = user.getId();

            // when
            List<MentorApplicationHistoryResponse> response = adminMentorApplicationService.findMentorApplicationHistory(withoutApplicationUserId);

            // then
            assertThat(response).isEmpty();
        }

        @Test
        void 응답에_지원서_상태와_거절_사유가_포함된다() {
            // given
            mentorApplicationFixture.거절된_멘토신청(user.getId(), UniversitySelectType.CATALOG, university.getId());
            mentorApplicationFixture.승인된_멘토신청(user.getId(), UniversitySelectType.CATALOG, university.getId());

            // when
            List<MentorApplicationHistoryResponse> response = adminMentorApplicationService.findMentorApplicationHistory(user.getId());

            // then
            assertAll(
                    () -> assertThat(response).hasSize(2),
                    () -> assertThat(response.get(0).mentorApplicationStatus()).isEqualTo(MentorApplicationStatus.APPROVED),
                    () -> assertThat(response.get(0).rejectedReason()).isNull(),
                    () -> assertThat(response.get(0).applicationOrder()).isEqualTo(2),
                    () -> assertThat(response.get(1).mentorApplicationStatus()).isEqualTo(MentorApplicationStatus.REJECTED),
                    () -> assertThat(response.get(1).rejectedReason()).isNotNull(),
                    () -> assertThat(response.get(1).applicationOrder()).isEqualTo(1)
            );
        }

        @Test
        void 존재하지_않는_사용자_이력을_조회하면_예외_응답을_반환한다() {
            // given
            long nonExistentUserId = 99999L;

            // when & then
            assertThatCode(() -> adminMentorApplicationService.findMentorApplicationHistory(nonExistentUserId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(USER_NOT_FOUND.getMessage());
        }
    }
}
