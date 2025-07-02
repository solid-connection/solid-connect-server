package com.example.solidconnection.mentor.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.dto.MentorDetailResponse;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class MentorQueryService {

    private final MentorRepository mentorRepository;
    private final MentoringRepository mentoringRepository;
    private final SiteUserRepository siteUserRepository;

    @Transactional(readOnly = true)
    public MentorDetailResponse getMentorDetails(long mentorId, SiteUser currentUser) {
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));
        SiteUser mentorUser = siteUserRepository.findById(mentor.getSiteUserId())
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));
        boolean isApplied = mentoringRepository.existsByMentorIdAndMenteeId(mentorId, currentUser.getId());

        return MentorDetailResponse.of(mentor, mentorUser, isApplied);
    }
}
