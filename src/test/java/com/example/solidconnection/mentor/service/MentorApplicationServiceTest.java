package com.example.solidconnection.mentor.service;


import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_APPLICATION_ALREADY_EXISTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.BDDMockito.given;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.ExchangePhase;
import com.example.solidconnection.mentor.domain.MentorApplication;
import com.example.solidconnection.mentor.dto.MentorApplicationRequest;
import com.example.solidconnection.mentor.repository.MentorApplicationRepository;
import com.example.solidconnection.s3.domain.ImgType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
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
        MentorApplicationRequest request = createMentorApplicationRequest();
        MockMultipartFile file = createMentorProofFile();
        String fileUrl = "/mentor-proof.pdf";
        given(s3Service.uploadFile(file, ImgType.MENTOR_PROOF))
                .willReturn(new UploadedFileUrlResponse(fileUrl));

        // when
        mentorApplicationService.submitMentorApplication(user.getId(), request, file);

        // then
        assertThat(mentorApplicationRepository.existsBySiteUserId(user.getId())).isEqualTo(true);
    }

    @Test
    void universityId_가_null_로_들어와도_멘토_승격_신청을_등록한다() {
        // given
        MentorApplicationRequest request = createMentorApplicationRequestWithoutUniversityId();
        MockMultipartFile file = createMentorProofFile();
        String fileUrl = "/mentor-proof.pdf";
        given(s3Service.uploadFile(file, ImgType.MENTOR_PROOF))
                .willReturn(new UploadedFileUrlResponse(fileUrl));

        // when
        mentorApplicationService.submitMentorApplication(user.getId(), request, file);

        // then
        assertThat(mentorApplicationRepository.existsBySiteUserId(user.getId())).isEqualTo(true);
    }

    @Test
    void 한_유저가_1회_이상_멘토_신청_요청을_보내면_예외() {
        // given
        MentorApplication existsMentorApplication = new MentorApplication(
                user,
                "미주권",
                "헝가리2",
                1L,
                "/mentor-proof.pdf",
                ExchangePhase.AFTER_EXCHANGE
        );
        mentorApplicationRepository.save(existsMentorApplication);

        MentorApplicationRequest request = createMentorApplicationRequest();
        MockMultipartFile file = createMentorProofFile();

        // when
        // then
        assertThatCode(() -> mentorApplicationService.submitMentorApplication(user.getId(), request, file))
                .isInstanceOf(CustomException.class)
                .hasMessage(MENTOR_APPLICATION_ALREADY_EXISTED.getMessage());
    }

    private MockMultipartFile createMentorProofFile() {
        return new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    private MentorApplicationRequest createMentorApplicationRequest() {
        return new MentorApplicationRequest(
                ExchangePhase.AFTER_EXCHANGE,
                "미주권",
                "헝가리2",
                1L
        );
    }

    private MentorApplicationRequest createMentorApplicationRequestWithoutUniversityId() {
        return new MentorApplicationRequest(
                ExchangePhase.AFTER_EXCHANGE,
                "미주권",
                "헝가리2",
                null
        );
    }


}
