package com.example.solidconnection.mentor.service;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.UNAUTHORIZED_MENTORING;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.mentor.dto.CheckMentoringRequest;
import com.example.solidconnection.mentor.dto.CheckedMentoringsResponse;
import com.example.solidconnection.mentor.dto.MentoringCountResponse;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentoringCheckService {

    private final MentorRepository mentorRepository;
    private final MentoringRepository mentoringRepository;

    @Transactional
    public CheckedMentoringsResponse checkMentoringsForMentor(long mentorUserId, CheckMentoringRequest checkMentoringRequest) {
        Mentor mentor = mentorRepository.findBySiteUserId(mentorUserId)
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));
        List<Mentoring> mentorings = mentoringRepository.findAllById(checkMentoringRequest.checkedMentoringIds());
        List<Long> actualMentorIds = mentorings.stream()
                .map(Mentoring::getMentorId)
                .distinct()
                .toList();

        mentorings.forEach(Mentoring::checkByMentor);
        validateMentoringsOwnership(actualMentorIds, mentor.getId());

        return CheckedMentoringsResponse.from(mentorings);
    }

    @Transactional
    public CheckedMentoringsResponse checkMentoringsForMentee(long menteeUserId, CheckMentoringRequest checkMentoringRequest) {
        List<Mentoring> mentorings = mentoringRepository.findAllById(checkMentoringRequest.checkedMentoringIds());
        List<Long> actualMenteeIds = mentorings.stream()
                .map(Mentoring::getMenteeId)
                .distinct()
                .toList();

        validateMentoringsOwnership(actualMenteeIds, menteeUserId);
        mentorings.forEach(Mentoring::checkByMentee);

        return CheckedMentoringsResponse.from(mentorings);
    }

    private void validateMentoringsOwnership(List<Long> actualOwnerIds, long expectedOwnerId) {
        actualOwnerIds.stream()
                .filter(actualOwnerId -> actualOwnerId != expectedOwnerId)
                .findFirst()
                .ifPresent(ownerId -> {
                    throw new CustomException(UNAUTHORIZED_MENTORING);
                });
    }

    @Transactional(readOnly = true)
    public MentoringCountResponse getUncheckedMentoringCount(long siteUserId) {
        Mentor mentor = mentorRepository.findBySiteUserId(siteUserId)
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));

        int count = mentoringRepository.countByMentorIdAndCheckedAtByMentorIsNull(mentor.getId());

        return MentoringCountResponse.from(count);
    }
}
