package com.example.solidconnection.mentor.service;

import static com.example.solidconnection.common.exception.ErrorCode.ALREADY_EXIST_MENTORING;
import static com.example.solidconnection.common.exception.ErrorCode.MENTORING_ALREADY_CONFIRMED;
import static com.example.solidconnection.common.exception.ErrorCode.MENTORING_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.UNAUTHORIZED_MENTORING;

import com.example.solidconnection.chat.service.ChatService;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.mentor.dto.MentoringApplyRequest;
import com.example.solidconnection.mentor.dto.MentoringApplyResponse;
import com.example.solidconnection.mentor.dto.MentoringConfirmRequest;
import com.example.solidconnection.mentor.dto.MentoringConfirmResponse;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentoringCommandService {

    private final MentoringRepository mentoringRepository;
    private final MentorRepository mentorRepository;
    private final ChatService chatService;

    @Transactional
    public MentoringApplyResponse applyMentoring(long siteUserId, MentoringApplyRequest mentoringApplyRequest) {
        long mentorSiteUserId = mentoringApplyRequest.siteUserId();

        Mentor mentor = mentorRepository.findBySiteUserId(mentorSiteUserId)
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));
        long mentorId = mentor.getId();

        if (mentoringRepository.existsByMentorIdAndMenteeId(mentorId, siteUserId)) {
            throw new CustomException(ALREADY_EXIST_MENTORING);
        }

        Mentoring mentoring = new Mentoring(mentorId, siteUserId, VerifyStatus.PENDING);
        return MentoringApplyResponse.from(mentoringRepository.save(mentoring));
    }

    @Transactional
    public MentoringConfirmResponse confirmMentoring(long siteUserId, long mentoringId, MentoringConfirmRequest mentoringConfirmRequest) {
        Mentoring mentoring = mentoringRepository.findById(mentoringId)
                .orElseThrow(() -> new CustomException(MENTORING_NOT_FOUND));

        Mentor mentor = mentorRepository.findBySiteUserId(siteUserId)
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));

        validateMentoringOwnership(mentor, mentoring);
        validateMentoringNotConfirmed(mentoring);

        mentoring.confirm(mentoringConfirmRequest.status());

        Long chatRoomId = null;
        if (mentoringConfirmRequest.status() == VerifyStatus.APPROVED) {
            mentor.increaseMenteeCount();
            chatRoomId = chatService.createMentoringChatRoom(mentoringId, mentor.getSiteUserId(), mentoring.getMenteeId());
        }

        return MentoringConfirmResponse.from(mentoring, chatRoomId);
    }

    private void validateMentoringNotConfirmed(Mentoring mentoring) {
        if (mentoring.getVerifyStatus() != VerifyStatus.PENDING) {
            throw new CustomException(MENTORING_ALREADY_CONFIRMED);
        }
    }

    // 멘토는 본인의 멘토링에 대해 confirm 및 check해야 한다.
    private void validateMentoringOwnership(Mentor mentor, Mentoring mentoring) {
        if (mentoring.getMentorId() != mentor.getId()) {
            throw new CustomException(UNAUTHORIZED_MENTORING);
        }
    }
}
