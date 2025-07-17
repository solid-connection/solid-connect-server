package com.example.solidconnection.mentor.service;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_NOT_FOUND;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.mentor.dto.MentoringCountResponse;
import com.example.solidconnection.mentor.dto.MentoringListResponse;
import com.example.solidconnection.mentor.dto.MentoringResponse;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentoringQueryService {

    private final MentoringRepository mentoringRepository;
    private final MentorRepository mentorRepository;
    private final SiteUserRepository siteUserRepository;

    @Transactional(readOnly = true)
    public MentoringListResponse getMentorings(long siteUserId) {
        Mentor mentor = mentorRepository.findBySiteUserId(siteUserId)
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));

        List<Mentoring> mentorings = mentoringRepository.findAllByMentorId(mentor.getId());
        List<MentoringResponse> mentoringResponses = mentorings.stream()
                .map(mentoring -> {
                    SiteUser mentee = siteUserRepository.findById(mentoring.getMenteeId())
                            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                    return MentoringResponse.from(mentoring, mentee);
                })
                .toList();

        return MentoringListResponse.from(mentoringResponses);
    }

    @Transactional(readOnly = true)
    public MentoringCountResponse getNewMentoringsCount(long siteUserId) {
        Mentor mentor = mentorRepository.findBySiteUserId(siteUserId)
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));

        int count = mentoringRepository.countByMentorIdAndCheckedAtIsNull(mentor.getId());

        return MentoringCountResponse.from(count);
    }
}
