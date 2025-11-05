package com.example.solidconnection.mentor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.mentor.domain.Channel;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.dto.ChannelResponse;
import com.example.solidconnection.mentor.dto.MentorDetailResponse;
import com.example.solidconnection.mentor.dto.MentorPreviewResponse;
import com.example.solidconnection.mentor.fixture.ChannelFixture;
import com.example.solidconnection.mentor.fixture.MentorFixture;
import com.example.solidconnection.mentor.fixture.MentoringFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.term.fixture.TermFixture;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.fixture.UniversityFixture;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@DisplayName("멘토 조회 서비스 테스트")
@TestContainerSpringBootTest
class MentorQueryServiceTest {

    @Autowired
    private MentorQueryService mentorQueryService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private MentorFixture mentorFixture;

    @Autowired
    private MentoringFixture mentoringFixture;

    @Autowired
    private ChannelFixture channelFixture;

    @Autowired
    private UniversityFixture universityFixture;

    @Autowired
    private TermFixture termFixture;

    private University university;

    @BeforeEach
    void setUp() {
        termFixture.현재_학기("2025-2");
        university = universityFixture.그라츠_대학();
    }

    @Nested
    class 멘토_단일_조회_성공 {

        @Test
        void 멘토_정보를_조회한다() {
            // given
            SiteUser siteUser = siteUserFixture.사용자();
            SiteUser mentorUser = siteUserFixture.사용자(1, "멘토");
            Mentor mentor = mentorFixture.멘토(mentorUser.getId(), university.getId());
            Channel channel1 = channelFixture.채널(1, mentor);
            Channel channel2 = channelFixture.채널(2, mentor);

            // when
            MentorDetailResponse response = mentorQueryService.getMentorDetails(mentor.getId(), siteUser.getId());

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

        @Test
        void 멘토에_대한_나의_멘토링_신청_여부를_조회한다() {
            // given
            SiteUser mentorUser = siteUserFixture.사용자(1, "멘토");
            Mentor mentor = mentorFixture.멘토(mentorUser.getId(), university.getId());

            SiteUser notAppliedUser = siteUserFixture.사용자(2, "멘토링 지원 안한 사용자");
            SiteUser appliedUser = siteUserFixture.사용자(3, "멘토링 지원한 사용자");
            mentoringFixture.대기중_멘토링(mentor.getId(), appliedUser.getId());

            // when
            MentorDetailResponse notAppliedResponse = mentorQueryService.getMentorDetails(mentor.getId(), notAppliedUser.getId());
            MentorDetailResponse appliedResponse = mentorQueryService.getMentorDetails(mentor.getId(), appliedUser.getId());

            // then
            assertAll(
                    () -> assertThat(notAppliedResponse.isApplied()).isFalse(),
                    () -> assertThat(appliedResponse.isApplied()).isTrue()
            );
        }
    }

    @Nested
    class 멘토_단일_조회_실패 {

        @Test
        void 존재하지_않는_멘토를_조회하면_예외가_발생한다() {
            // given
            long notExistingMentorId = 999L;

            // when & then
            assertThatCode(() -> mentorQueryService.getMentorDetails(notExistingMentorId, siteUserFixture.사용자().getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.MENTOR_NOT_FOUND.getMessage());
        }

        @Test
        void 임시멘토를_조회하면_예외가_발생한다() {
            // given
            SiteUser tempMentorUser = siteUserFixture.임시멘토();
            Mentor tempMentor = mentorFixture.임시멘토(tempMentorUser.getId(), university.getId());

            // when & then
            assertThatCode(() -> mentorQueryService.getMentorDetails(tempMentor.getId(), siteUserFixture.사용자().getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.MENTOR_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 멘토_미리보기_목록_정보_조회 {

        private static final int NO_NEXT_PAGE_NUMBER = -1;

        private Mentor mentor1, mentor2;
        private SiteUser mentorUser1, mentorUser2, currentUser;
        private University university1, university2;

        @BeforeEach
        void setUp() {
            currentUser = siteUserFixture.사용자(1, "사용자1");
            mentorUser1 = siteUserFixture.사용자(2, "멘토1");
            mentorUser2 = siteUserFixture.사용자(3, "멘토2");
            university1 = universityFixture.괌_대학();
            university2 = universityFixture.린츠_카톨릭_대학();
            mentor1 = mentorFixture.멘토(mentorUser1.getId(), university1.getId());
            mentor2 = mentorFixture.멘토(mentorUser2.getId(), university2.getId());
        }

        @Test
        void 멘토_미리보기_목록의_정보를_조회한다() {
            // given
            Channel channel1 = channelFixture.채널(1, mentor1);
            Channel channel2 = channelFixture.채널(2, mentor2);

            // when
            SliceResponse<MentorPreviewResponse> response = mentorQueryService.getMentorPreviews("", currentUser.getId(), PageRequest.of(0, 10));

            // then
            Map<Long, MentorPreviewResponse> mentorPreviewMap = response.content().stream()
                    .collect(Collectors.toMap(MentorPreviewResponse::id, Function.identity()));
            MentorPreviewResponse mentor1Response = mentorPreviewMap.get(mentor1.getId());
            MentorPreviewResponse mentor2Response = mentorPreviewMap.get(mentor2.getId());
            assertAll(
                    () -> assertThat(mentor1Response.nickname()).isEqualTo(mentorUser1.getNickname()),
                    () -> assertThat(mentor1Response.universityName()).isEqualTo(university1.getKoreanName()),
                    () -> assertThat(mentor1Response.country()).isEqualTo(university1.getCountry().getKoreanName()),
                    () -> assertThat(mentor1Response.channels()).extracting(ChannelResponse::url)
                            .containsOnly(channel1.getUrl()),

                    () -> assertThat(mentor2Response.nickname()).isEqualTo(mentorUser2.getNickname()),
                    () -> assertThat(mentor2Response.universityName()).isEqualTo(university2.getKoreanName()),
                    () -> assertThat(mentor2Response.country()).isEqualTo(university2.getCountry().getKoreanName()),
                    () -> assertThat(mentor2Response.channels()).extracting(ChannelResponse::url)
                            .containsOnly(channel2.getUrl())
            );
        }

        @Test
        void 임시멘토는_미리보기_목록에서_제외된다() {
            // given
            SiteUser tempMentorUser = siteUserFixture.임시멘토();
            Mentor tempMentor = mentorFixture.임시멘토(tempMentorUser.getId(), university1.getId());
            Channel channel1 = channelFixture.채널(1, mentor1);
            Channel channel2 = channelFixture.채널(2, mentor2);

            // when
            SliceResponse<MentorPreviewResponse> response = mentorQueryService.getMentorPreviews("", currentUser.getId(), PageRequest.of(0, 10));

            // then
            Map<Long, MentorPreviewResponse> mentorPreviewMap = response.content().stream()
                    .collect(Collectors.toMap(MentorPreviewResponse::id, Function.identity()));
            MentorPreviewResponse mentor1Response = mentorPreviewMap.get(mentor1.getId());
            MentorPreviewResponse mentor2Response = mentorPreviewMap.get(mentor2.getId());
            assertThat(mentorPreviewMap.get(tempMentor.getId())).isNull();
            assertAll(
                    () -> assertThat(mentor1Response.nickname()).isEqualTo(mentorUser1.getNickname()),
                    () -> assertThat(mentor1Response.universityName()).isEqualTo(university1.getKoreanName()),
                    () -> assertThat(mentor1Response.country()).isEqualTo(university1.getCountry().getKoreanName()),
                    () -> assertThat(mentor1Response.channels()).extracting(ChannelResponse::url)
                            .containsOnly(channel1.getUrl()),

                    () -> assertThat(mentor2Response.nickname()).isEqualTo(mentorUser2.getNickname()),
                    () -> assertThat(mentor2Response.universityName()).isEqualTo(university2.getKoreanName()),
                    () -> assertThat(mentor2Response.country()).isEqualTo(university2.getCountry().getKoreanName()),
                    () -> assertThat(mentor2Response.channels()).extracting(ChannelResponse::url)
                            .containsOnly(channel2.getUrl())
            );
        }

        @Test
        void 다음_페이지_번호를_응답한다() {
            // given
            SliceResponse<MentorPreviewResponse> response = mentorQueryService.getMentorPreviews("", currentUser.getId(), PageRequest.of(0, 1));

            // then
            assertThat(response.nextPageNumber()).isEqualTo(2);
        }

        @Test
        void 다음_페이지가_없으면_페이지_없음을_의미하는_값을_응답한다() {
            // given
            SliceResponse<MentorPreviewResponse> response = mentorQueryService.getMentorPreviews("", currentUser.getId(), PageRequest.of(0, 10));

            // then
            assertThat(response.nextPageNumber()).isEqualTo(NO_NEXT_PAGE_NUMBER);
        }
    }

    @Nested
    class 멘토_미리보기_목록_필터링 {

        private Mentor asiaMentor, europeMentor;
        private SiteUser currentUser;
        private University asiaUniversity, europeUniversity;

        @BeforeEach
        void setUp() {
            currentUser = siteUserFixture.사용자(1, "사용자1");
            SiteUser mentorUser1 = siteUserFixture.사용자(2, "멘토1");
            SiteUser mentorUser2 = siteUserFixture.사용자(3, "멘토2");
            asiaUniversity = universityFixture.메이지_대학();
            europeUniversity = universityFixture.린츠_카톨릭_대학();
            asiaMentor = mentorFixture.멘토(mentorUser1.getId(), asiaUniversity.getId());
            europeMentor = mentorFixture.멘토(mentorUser2.getId(), europeUniversity.getId());
        }

        @Test
        void 권역으로_멘토_목록을_필터링한다() {
            // when
            SliceResponse<MentorPreviewResponse> asiaFilteredResponse = mentorQueryService.getMentorPreviews(
                    asiaUniversity.getRegion().getKoreanName(), currentUser.getId(), PageRequest.of(0, 10));
            SliceResponse<MentorPreviewResponse> europeFilteredResponse = mentorQueryService.getMentorPreviews(
                    europeUniversity.getRegion().getKoreanName(), currentUser.getId(), PageRequest.of(0, 10));

            // then
            assertAll(
                    () -> assertThat(asiaFilteredResponse.content()).hasSize(1)
                            .extracting(MentorPreviewResponse::id)
                            .containsExactly(asiaMentor.getId()),
                    () -> assertThat(europeFilteredResponse.content()).hasSize(1)
                            .extracting(MentorPreviewResponse::id)
                            .containsExactly(europeMentor.getId())
            );
        }

        @Test
        void 권역으로_멘토_목록을_필터링_할때_임시멘토는_제외된다() {
            // when
            University americaUniversity = universityFixture.네바다주립_대학_라스베이거스();
            SiteUser tempMentorUser = siteUserFixture.임시멘토();
            Mentor tempMentor = mentorFixture.임시멘토(tempMentorUser.getId(), americaUniversity.getId());

            SliceResponse<MentorPreviewResponse> asiaFilteredResponse = mentorQueryService.getMentorPreviews(
                    asiaUniversity.getRegion().getKoreanName(), currentUser.getId(), PageRequest.of(0, 10));
            SliceResponse<MentorPreviewResponse> europeFilteredResponse = mentorQueryService.getMentorPreviews(
                    europeUniversity.getRegion().getKoreanName(), currentUser.getId(), PageRequest.of(0, 10));
            SliceResponse<MentorPreviewResponse> americaFilteredResponse = mentorQueryService.getMentorPreviews(
                    americaUniversity.getRegion().getKoreanName(), currentUser.getId(), PageRequest.of(0, 10));

            // then
            assertAll(
                    () -> assertThat(americaFilteredResponse.content()).isEmpty(),
                    () -> assertThat(asiaFilteredResponse.content()).hasSize(1)
                            .extracting(MentorPreviewResponse::id)
                            .containsExactly(asiaMentor.getId()),
                    () -> assertThat(europeFilteredResponse.content()).hasSize(1)
                            .extracting(MentorPreviewResponse::id)
                            .containsExactly(europeMentor.getId())
            );
        }

        @Test
        void 권역을_지정하지_않으면_전체_멘토_목록을_조회한다() {
            // when
            SliceResponse<MentorPreviewResponse> response = mentorQueryService.getMentorPreviews("", currentUser.getId(), PageRequest.of(0, 10));

            // then
            assertThat(response.content()).hasSize(2)
                    .extracting(MentorPreviewResponse::id)
                    .containsExactlyInAnyOrder(asiaMentor.getId(), europeMentor.getId());
        }
    }
}