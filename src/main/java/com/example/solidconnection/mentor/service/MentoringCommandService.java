package com.example.solidconnection.mentor.service;

import com.example.solidconnection.application.domain.VerifyStatus;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.mentor.dto.MentoringApplyRequest;
import com.example.solidconnection.mentor.dto.MentoringApplyResponse;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.solidconnection.common.exception.ErrorCode.ALREADY_MENTOR;
import static com.example.solidconnection.common.exception.ErrorCode.SELF_MENTORING_NOT_ALLOWED;

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
}
