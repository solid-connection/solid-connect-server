package com.example.solidconnection.mentor.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.dto.MentorMyPageResponse;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class MentorMyPageService {

    private final MentorRepository mentorRepository;

    @Transactional(readOnly = true)
    public MentorMyPageResponse getMentorMyPage(SiteUser siteUser) {
        Mentor mentor = mentorRepository.findBySiteUserId(siteUser.getId())
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));
        return MentorMyPageResponse.of(mentor, siteUser);
    }
}
