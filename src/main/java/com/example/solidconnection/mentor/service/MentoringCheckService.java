package com.example.solidconnection.mentor.service;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_NOT_FOUND;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.dto.MentoringCountResponse;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentoringCheckService {

    private final MentorRepository mentorRepository;
    private final MentoringRepository mentoringRepository;

    @Transactional(readOnly = true)
    public MentoringCountResponse getUncheckedMentoringCount(long siteUserId) {
        Mentor mentor = mentorRepository.findBySiteUserId(siteUserId)
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));

        int count = mentoringRepository.countByMentorIdAndCheckedAtByMentorIsNull(mentor.getId());

        return MentoringCountResponse.from(count);
    }
}
