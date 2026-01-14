package com.example.solidconnection.mentor.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.MentorApplication;
import com.example.solidconnection.mentor.domain.MentorApplicationStatus;
import com.example.solidconnection.mentor.dto.MentorApplicationRequest;
import com.example.solidconnection.mentor.repository.MentorApplicationRepository;
import com.example.solidconnection.s3.domain.UploadType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.repository.TermRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_APPLICATION_ALREADY_EXISTED;
import static com.example.solidconnection.common.exception.ErrorCode.TERM_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorApplicationService {

    private final MentorApplicationRepository mentorApplicationRepository;
    private final SiteUserRepository siteUserRepository;
    private final S3Service s3Service;
    private final TermRepository termRepository;

    @Transactional
    public void submitMentorApplication(
            long siteUserId,
            MentorApplicationRequest mentorApplicationRequest,
            MultipartFile file
    ) {
        ensureNoPendingOrApprovedMentorApplication(siteUserId);

        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Term term = termRepository.findByName(mentorApplicationRequest.term())
                .orElseThrow(() -> new CustomException(TERM_NOT_FOUND));
        UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(file, UploadType.MENTOR_PROOF);
        MentorApplication mentorApplication = new MentorApplication(
                siteUser.getId(),
                mentorApplicationRequest.country(),
                mentorApplicationRequest.universityId(),
                mentorApplicationRequest.universitySelectType(),
                uploadedFile.fileUrl(),
                term.getId(),
                mentorApplicationRequest.exchangeStatus()
        );
        mentorApplicationRepository.save(mentorApplication);
    }

    private void ensureNoPendingOrApprovedMentorApplication(long siteUserId) {
        if (mentorApplicationRepository.existsBySiteUserIdAndMentorApplicationStatusIn(
                siteUserId,
                List.of(MentorApplicationStatus.PENDING, MentorApplicationStatus.APPROVED))
        ) {
            throw new CustomException(MENTOR_APPLICATION_ALREADY_EXISTED);
        }
    }
}
