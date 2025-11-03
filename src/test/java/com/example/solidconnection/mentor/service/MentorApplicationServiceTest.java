package com.example.solidconnection.mentor.service;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_APPLICATION_ALREADY_EXISTED;
import static com.example.solidconnection.common.exception.ErrorCode.UNIVERSITY_ID_MUST_BE_NULL_FOR_OTHER;
import static com.example.solidconnection.common.exception.ErrorCode.UNIVERSITY_ID_REQUIRED_FOR_CATALOG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.BDDMockito.given;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.MentorApplicationStatus;
import com.example.solidconnection.mentor.domain.UniversitySelectType;
import com.example.solidconnection.mentor.dto.MentorApplicationRequest;
import com.example.solidconnection.mentor.fixture.MentorApplicationFixture;
import com.example.solidconnection.mentor.repository.MentorApplicationRepository;
import com.example.solidconnection.s3.domain.ImgType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import com.example.solidconnection.siteuser.domain.ExchangeStatus;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

@TestContainerSpringBootTest
@DisplayName("멘토 승격 신청 서비스 테스트")
public class MentorApplicationServiceTest {

    @Autowired
    private MentorApplicationService mentorApplicationService;

    @Autowired
    private MentorApplicationRepository mentorApplicationRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private MentorApplicationFixture mentorApplicationFixture;

    @MockBean
    private S3Service s3Service;

    private SiteUser user;

    @BeforeEach
    void setUp() {
        user = siteUserFixture.사용자();
    }

    @Test
    void 멘토_승격_신청을_등록한다() {
        // given
        UniversitySelectType universitySelectType = UniversitySelectType.CATALOG;
        Long universityId = 1L;
        MentorApplicationRequest request = createMentorApplicationRequest(universitySelectType, universityId);
        MockMultipartFile file = createMentorProofFile();
        String fileUrl = "/mentor-proof.pdf";
        given(s3Service.uploadFile(file, ImgType.MENTOR_PROOF))
                .willReturn(new UploadedFileUrlResponse(fileUrl));

        // when
        mentorApplicationService.submitMentorApplication(user.getId(), request, file);

        // then
        assertThat(mentorApplicationRepository.existsBySiteUserIdAndMentorApplicationStatusIn(user.getId(), List.of(MentorApplicationStatus.PENDING, MentorApplicationStatus.APPROVED))).isEqualTo(true);
    }

    @Test
    void universityId_가_null_로_들어와도_멘토_승격_신청을_등록한다() {
        // given
        UniversitySelectType universitySelectType = UniversitySelectType.OTHER;
        Long universityId = null;
        MentorApplicationRequest request = createMentorApplicationRequest(universitySelectType, universityId);
        MockMultipartFile file = createMentorProofFile();
        String fileUrl = "/mentor-proof.pdf";
        given(s3Service.uploadFile(file, ImgType.MENTOR_PROOF))
                .willReturn(new UploadedFileUrlResponse(fileUrl));

        // when
        mentorApplicationService.submitMentorApplication(user.getId(), request, file);

        // then
        assertThat(mentorApplicationRepository.existsBySiteUserIdAndMentorApplicationStatusIn(user.getId(),List.of(MentorApplicationStatus.PENDING, MentorApplicationStatus.APPROVED))).isEqualTo(true);
    }

    @Test
    void 대학_선택_타입이_CATALOG_인데_universityId가_null_이면_예외가_발생한다(){
        // given
        UniversitySelectType universitySelectType = UniversitySelectType.CATALOG;
        Long universityId = null;
        MentorApplicationRequest request = createMentorApplicationRequest(universitySelectType, universityId);
        MockMultipartFile file = createMentorProofFile();
        String fileUrl = "/mentor-proof.pdf";
        given(s3Service.uploadFile(file, ImgType.MENTOR_PROOF))
                .willReturn(new UploadedFileUrlResponse(fileUrl));

        // when & then
        assertThatCode(() -> mentorApplicationService.submitMentorApplication(user.getId(), request, file))
                .isInstanceOf(CustomException.class)
                .hasMessage(UNIVERSITY_ID_REQUIRED_FOR_CATALOG.getMessage());
    }

    @Test
    void 대학_선택_타입이_OTHER_인데_universityId가_존재하면_예외가_발생한다(){
        // given
        UniversitySelectType universitySelectType = UniversitySelectType.OTHER;
        Long universityId = 1L;
        MentorApplicationRequest request = createMentorApplicationRequest(universitySelectType, universityId);
        MockMultipartFile file = createMentorProofFile();
        String fileUrl = "/mentor-proof.pdf";
        given(s3Service.uploadFile(file, ImgType.MENTOR_PROOF))
                .willReturn(new UploadedFileUrlResponse(fileUrl));

        // when & then
        assertThatCode(() -> mentorApplicationService.submitMentorApplication(user.getId(), request, file))
                .isInstanceOf(CustomException.class)
                .hasMessage(UNIVERSITY_ID_MUST_BE_NULL_FOR_OTHER.getMessage());
    }

    @Test
    void 이미_PENDING_상태인_멘토_승격_요청이_존재할_때_중복으로_멘토_승격_신청_시_예외가_발생한다() {
        // given
        mentorApplicationFixture.대기중_멘토신청(user.getId(), UniversitySelectType.CATALOG, 1L);

        UniversitySelectType universitySelectType = UniversitySelectType.CATALOG;
        Long universityId = 1L;
        MentorApplicationRequest request = createMentorApplicationRequest(universitySelectType, universityId);
        MockMultipartFile file = createMentorProofFile();

        // when & then
        assertThatCode(() -> mentorApplicationService.submitMentorApplication(user.getId(), request, file))
                .isInstanceOf(CustomException.class)
                .hasMessage(MENTOR_APPLICATION_ALREADY_EXISTED.getMessage());
    }

    @Test
    void 이미_APPROVE_상태인_멘토_승격_요청이_존재할_때_중복으로_멘토_승격_신청_시_예외가_발생한다() {
        // given
        mentorApplicationFixture.승인된_멘토신청(user.getId(), UniversitySelectType.CATALOG, 1L);

        UniversitySelectType universitySelectType = UniversitySelectType.CATALOG;
        Long universityId = 1L;
        MentorApplicationRequest request = createMentorApplicationRequest(universitySelectType, universityId);
        MockMultipartFile file = createMentorProofFile();

        // when & then
        assertThatCode(() -> mentorApplicationService.submitMentorApplication(user.getId(), request, file))
                .isInstanceOf(CustomException.class)
                .hasMessage(MENTOR_APPLICATION_ALREADY_EXISTED.getMessage());
    }

    @Test
    void 이미_REJECTED_상태인_멘토_승격_요청이_존재할_때_멘토_신청이_등록된다() {
        // given
        mentorApplicationFixture.거절된_멘토신청(user.getId(), UniversitySelectType.CATALOG, 1L);

        UniversitySelectType universitySelectType = UniversitySelectType.CATALOG;
        Long universityId = 1L;
        MentorApplicationRequest request = createMentorApplicationRequest(universitySelectType, universityId);
        MockMultipartFile file = createMentorProofFile();
        String fileUrl = "/mentor-proof.pdf";
        given(s3Service.uploadFile(file, ImgType.MENTOR_PROOF))
                .willReturn(new UploadedFileUrlResponse(fileUrl));

        // when
        mentorApplicationService.submitMentorApplication(user.getId(), request, file);

        // then
        assertThat(mentorApplicationRepository.existsBySiteUserIdAndMentorApplicationStatusIn(user.getId(),List.of(MentorApplicationStatus.PENDING, MentorApplicationStatus.APPROVED))).isEqualTo(true);
    }

    private MockMultipartFile createMentorProofFile() {
        return new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    private MentorApplicationRequest createMentorApplicationRequest(UniversitySelectType universitySelectType, Long universityId) {
        return new MentorApplicationRequest(
                ExchangeStatus.AFTER_EXCHANGE,
                universitySelectType,
                "US",
                universityId
        );
    }
}
