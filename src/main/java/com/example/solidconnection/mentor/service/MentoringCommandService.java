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

import static com.example.solidconnection.common.exception.ErrorCode.MENTORING_ALREADY_CONFIRMED;
import static com.example.solidconnection.common.exception.ErrorCode.MENTORING_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.REJECTED_REASON_REQUIRED;
import static com.example.solidconnection.common.exception.ErrorCode.UNAUTHORIZED_MENTORING;

@Service
@RequiredArgsConstructor
public class MentoringCommandService {

    private final MentoringRepository mentoringRepository;
    private final MentorRepository mentorRepository;

    @Transactional
    public MentoringApplyResponse applyMentoring(long siteUserId, MentoringApplyRequest mentoringApplyRequest) {
        Mentoring mentoring = new Mentoring(mentoringApplyRequest.mentorId(), siteUserId, VerifyStatus.PENDING);

        return MentoringApplyResponse.from(mentoringRepository.save(mentoring));
    }

    @Transactional
    public MentoringConfirmResponse confirmMentoring(long siteUserId, long mentoringId, MentoringConfirmRequest mentoringConfirmRequest) {
        Mentoring mentoring = mentoringRepository.findById(mentoringId)
                .orElseThrow(() -> new CustomException(MENTORING_NOT_FOUND));

        Mentor mentor = mentorRepository.findBySiteUserId(siteUserId)
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));

        validateMentoringOwnership(mentor, mentoring);
        validateMentoringPermission(mentoring);

        if (mentoringConfirmRequest.status() == VerifyStatus.REJECTED
                && (mentoringConfirmRequest.rejectedReason() == null || mentoringConfirmRequest.rejectedReason().isBlank())) {
            throw new CustomException(REJECTED_REASON_REQUIRED);
        }

        mentoring.confirm(mentoringConfirmRequest.status(), mentoringConfirmRequest.rejectedReason());

        if (mentoringConfirmRequest.status() == VerifyStatus.APPROVED) {
            mentor.increaseMenteeCount();
        }

        return MentoringConfirmResponse.from(mentoring);
    }

    private void validateMentoringPermission(Mentoring mentoring) {
        if (mentoring.getVerifyStatus() != VerifyStatus.PENDING) {
            throw new CustomException(MENTORING_ALREADY_CONFIRMED);
        }
    }

    @Transactional
    public MentoringCheckResponse checkMentoring(long siteUserId, long mentoringId) {
        Mentoring mentoring = mentoringRepository.findById(mentoringId)
                .orElseThrow(() -> new CustomException(MENTORING_NOT_FOUND));

        Mentor mentor = mentorRepository.findBySiteUserId(siteUserId)
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));

        validateMentoringOwnership(mentor, mentoring);

        mentoring.check();

        return MentoringCheckResponse.from(mentoring.getId());
    }

    // 멘토는 본인의 멘토링에 대해 confirm 및 check해야 한다.
    private void validateMentoringOwnership(Mentor mentor, Mentoring mentoring) {
        if (mentoring.getMentorId() != mentor.getId()) {
            throw new CustomException(UNAUTHORIZED_MENTORING);
        }
    }
}
