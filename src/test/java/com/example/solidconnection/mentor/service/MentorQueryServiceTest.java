package com.example.solidconnection.mentor.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

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

    private long universityId = 1L; // todo: 멘토 인증 기능 추가 변경 필요
    private String region = "아시아";

    @Nested
    class 멘토_단일_조회_성공 {

        @Test
        void 멘토_정보를_조회한다() {
            // given
            SiteUser siteUser = siteUserFixture.사용자();
            SiteUser mentorUser = siteUserFixture.사용자(1, "멘토");
            Mentor mentor = mentorFixture.멘토(mentorUser.getId(), universityId);
            Channel channel1 = channelFixture.채널(1, mentor);
            Channel channel2 = channelFixture.채널(2, mentor);

            // when
            MentorDetailResponse response = mentorQueryService.getMentorDetails(mentor.getId(), siteUser);

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(mentor.getId()),
                    () -> assertThat(response.nickname()).isEqualTo(mentorUser.getNickname()),
                    () -> assertThat(response.channels()).extracting(ChannelResponse::url)
                            .containsExactly(channel1.getUrl(), channel2.getUrl())
            );
        }

        @Test
        void 멘토에_대한_나의_멘토링_신청_여부를_조회한다() {
            // given
            SiteUser mentorUser = siteUserFixture.사용자(1, "멘토");
            Mentor mentor = mentorFixture.멘토(mentorUser.getId(), universityId);

            SiteUser notAppliedUser = siteUserFixture.사용자(2, "멘토링 지원 안한 사용자");
            SiteUser appliedUser = siteUserFixture.사용자(3, "멘토링 지원한 사용자");
            mentoringFixture.대기중_멘토링(mentor.getId(), appliedUser.getId());

            // when
            MentorDetailResponse notAppliedResponse = mentorQueryService.getMentorDetails(mentor.getId(), notAppliedUser);
            MentorDetailResponse appliedResponse = mentorQueryService.getMentorDetails(mentor.getId(), appliedUser);

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
        void 존재하지_않는_멘토를_조회하면_예외_응답을_반환한다() {
            // given
            long notExistingMentorId = 999L;

            // when & then
            assertThatCode(() -> mentorQueryService.getMentorDetails(notExistingMentorId, siteUserFixture.사용자()))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.MENTOR_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 멘토_미리보기_목록_조회 {

        private static final int NO_NEXT_PAGE_NUMBER = -1;

        private Mentor mentor1, mentor2;
        private SiteUser mentorUser1, mentorUser2, currentUser;

        @BeforeEach
        void setUp() {
            currentUser = siteUserFixture.사용자(1, "사용자1");
            mentorUser1 = siteUserFixture.사용자(2, "멘토1");
            mentorUser2 = siteUserFixture.사용자(3, "멘토2");
            mentor1 = mentorFixture.멘토(mentorUser1.getId(), universityId);
            mentor2 = mentorFixture.멘토(mentorUser2.getId(), universityId);
        }

        @Test
        void 멘토_미리보기_목록의_정보를_조회한다() {
            // given
            Channel channel1 = channelFixture.채널(1, mentor1);
            Channel channel2 = channelFixture.채널(2, mentor2);

            // when
            SliceResponse<MentorPreviewResponse> response = mentorQueryService.getMentorPreviews(region, currentUser, PageRequest.of(0, 10));

            // then
            Map<Long, MentorPreviewResponse> mentorPreviewMap = response.content().stream()
                    .collect(Collectors.toMap(MentorPreviewResponse::id, Function.identity()));

            assertAll(
                    () -> assertThat(mentorPreviewMap.get(mentor1.getId())).extracting(MentorPreviewResponse::nickname)
                            .isEqualTo(mentorUser1.getNickname()),
                    () -> assertThat(mentorPreviewMap.get(mentor1.getId()).channels()).extracting(ChannelResponse::url)
                            .containsOnly(channel1.getUrl()),
                    () -> assertThat(mentorPreviewMap.get(mentor2.getId())).extracting(MentorPreviewResponse::nickname)
                            .isEqualTo(mentorUser2.getNickname()),
                    () -> assertThat(mentorPreviewMap.get(mentor2.getId()).channels()).extracting(ChannelResponse::url)
                            .containsOnly(channel2.getUrl())
            );
        }

        @Test
        void 멘토들에_대한_나의_멘토링_지원_여부를_조회한다() {
            // given
            mentoringFixture.대기중_멘토링(mentor1.getId(), currentUser.getId());

            // when
            SliceResponse<MentorPreviewResponse> response = mentorQueryService.getMentorPreviews(region, currentUser, PageRequest.of(0, 10));

            // then
            Map<Long, MentorPreviewResponse> mentorPreviewMap = response.content().stream()
                    .collect(Collectors.toMap(MentorPreviewResponse::id, Function.identity()));
            assertAll(
                    () -> assertThat(mentorPreviewMap.get(mentor1.getId()).isApplied()).isTrue(),
                    () -> assertThat(mentorPreviewMap.get(mentor2.getId()).isApplied()).isFalse()
            );
        }

        @Test
        void 다음_페이지_번호를_응답한다() {
            // given
            SliceResponse<MentorPreviewResponse> response = mentorQueryService.getMentorPreviews(region, currentUser, PageRequest.of(0, 1));

            // then
            assertThat(response.nextPageNumber()).isEqualTo(2);
        }

        @Test
        void 다음_페이지가_없으면_페이지_없음을_의미하는_값을_응답한다() {
            // given
            SliceResponse<MentorPreviewResponse> response = mentorQueryService.getMentorPreviews(region, currentUser, PageRequest.of(0, 10));

            // then
            assertThat(response.nextPageNumber()).isEqualTo(NO_NEXT_PAGE_NUMBER);
        }
    }
}
