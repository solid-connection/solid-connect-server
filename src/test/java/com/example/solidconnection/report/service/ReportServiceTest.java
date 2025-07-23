package com.example.solidconnection.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.community.board.fixture.BoardFixture;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.fixture.PostFixture;
import com.example.solidconnection.report.domain.ReasonType;
import com.example.solidconnection.report.domain.TargetType;
import com.example.solidconnection.report.dto.ReportRequest;
import com.example.solidconnection.report.fixture.ReportFixture;
import com.example.solidconnection.report.repository.ReportRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("신고 서비스 테스트")
@TestContainerSpringBootTest
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private BoardFixture boardFixture;

    @Autowired
    private PostFixture postFixture;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private ReportFixture reportFixture;

    private SiteUser siteUser;
    private Post post;

    @BeforeEach
    void setUp() {
        siteUser = siteUserFixture.사용자();
        Board board = boardFixture.자유게시판();
        post = postFixture.게시글(board, siteUser);
    }

    @Nested
    class 신고_생성 {

        @Test
        void 정상적으로_신고한다() {
            // given
            ReportRequest request = new ReportRequest(post.getId(), TargetType.POST, ReasonType.INSULT);

            // when
            reportService.createReport(siteUser.getId(), request);

            // then
            boolean isSaved = reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                    siteUser.getId(), TargetType.POST, post.getId());
            assertThat(isSaved).isTrue();
        }

        @Test
        void 신고_대상이_존재하지_않으면_예외가_발생한다() {
            // given
            long notExistingId = 999L;
            ReportRequest request = new ReportRequest(notExistingId, TargetType.POST, ReasonType.INSULT);

            // when & then
            assertThatCode(() -> reportService.createReport(siteUser.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.REPORT_TARGET_NOT_FOUND.getMessage());
        }

        @Test
        void 이미_신고한_경우_예외가_발생한다() {
            // given
            reportFixture.신고(siteUser.getId(), TargetType.POST, post.getId());
            ReportRequest request = new ReportRequest(post.getId(), TargetType.POST, ReasonType.INSULT);

            // when & then
            assertThatCode(() -> reportService.createReport(siteUser.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.ALREADY_REPORTED_BY_CURRENT_USER.getMessage());
        }
    }
}
