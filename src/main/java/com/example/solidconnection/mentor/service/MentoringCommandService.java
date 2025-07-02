package com.example.solidconnection.mentor.service;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.mentor.dto.MentoringApplyRequest;
import com.example.solidconnection.mentor.dto.MentoringApplyResponse;
import com.example.solidconnection.mentor.dto.MentoringCheckResponse;
import com.example.solidconnection.mentor.dto.MentoringConfirmRequest;
import com.example.solidconnection.mentor.dto.MentoringConfirmResponse;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.solidconnection.common.exception.ErrorCode.ALREADY_MENTOR;
import static com.example.solidconnection.common.exception.ErrorCode.MENTORING_ALREADY_CONFIRMED;
import static com.example.solidconnection.common.exception.ErrorCode.MENTORING_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.REJECTED_REASON_REQUIRED;
import static com.example.solidconnection.common.exception.ErrorCode.SELF_MENTORING_NOT_ALLOWED;
import static com.example.solidconnection.common.exception.ErrorCode.UNAUTHORIZED_MENTORING;

@Service
@RequiredArgsConstructor
public class MentoringCommandService {

    private final MentoringRepository mentoringRepository;
    private final MentorRepository mentorRepository;

    @Transactional
    public MentoringApplyResponse applyMentoring(Long siteUserId, MentoringApplyRequest mentoringApplyRequest) {
        validateSelfMentoring(siteUserId, mentoringApplyRequest);
        validateAlreadyMentor(siteUserId);

        Mentoring mentoring = Mentoring.builder()
                .mentorId(mentoringApplyRequest.mentorId())
                .menteeId(siteUserId)
                .verifyStatus(VerifyStatus.PENDING)
                .build();

        return MentoringApplyResponse.from(
                mentoringRepository.save(mentoring)
        );
    }

    @Transactional
    public MentoringConfirmResponse confirmMentoring(Long siteUserId, Long mentoringId, MentoringConfirmRequest mentoringConfirmRequest) {
        Mentoring mentoring = mentoringRepository.findById(mentoringId)
                .orElseThrow(() -> new CustomException(MENTORING_NOT_FOUND));

        Mentor mentor = mentorRepository.findById(mentoring.getMentorId())
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));

        validateUnauthorizedMentoring(siteUserId, mentor);
        validateAlreadyConfirmed(mentoring);

        if (mentoringConfirmRequest.status() == VerifyStatus.APPROVED) {
            mentor.increaseMenteeCount();
        }
        else if (mentoringConfirmRequest.status() == VerifyStatus.REJECTED
                && (mentoringConfirmRequest.rejectedReason() == null || mentoringConfirmRequest.rejectedReason().isBlank())) {
            throw new CustomException(REJECTED_REASON_REQUIRED);
        }

        mentoring.confirm(mentoringConfirmRequest.status(), mentoringConfirmRequest.rejectedReason());

        return MentoringConfirmResponse.from(mentoring);
    }

    @Transactional
    public MentoringCheckResponse checkMentoring(Long siteUserId, Long mentoringId) {
        Mentoring mentoring = mentoringRepository.findById(mentoringId)
                .orElseThrow(() -> new CustomException(MENTORING_NOT_FOUND));

        Mentor mentor = mentorRepository.findById(mentoring.getMentorId())
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));

        validateUnauthorizedMentoring(siteUserId, mentor);

        mentoring.check();

        return MentoringCheckResponse.from(mentoring.getId());
    }

    private void validateSelfMentoring(Long siteUserId, MentoringApplyRequest request) {
        if (siteUserId.equals(request.mentorId())) {
            throw new CustomException(SELF_MENTORING_NOT_ALLOWED);
        }
    }

    private void validateAlreadyMentor(Long siteUserId) {
        if (mentorRepository.existsById(siteUserId)) {
            throw new CustomException(ALREADY_MENTOR);
        }
    }

    // 멘토는 본인의 멘토링에 대해 confirm 및 check해야 한다.
    private void validateUnauthorizedMentoring(Long siteUserId, Mentor mentor) {
        if (!siteUserId.equals(mentor.getSiteUserId())) {
            throw new CustomException(UNAUTHORIZED_MENTORING);
        }
    }

    private void validateAlreadyConfirmed(Mentoring mentoring) {
        if (mentoring.getVerifyStatus() != VerifyStatus.PENDING) {
            throw new CustomException(MENTORING_ALREADY_CONFIRMED);
        }
    }
}
