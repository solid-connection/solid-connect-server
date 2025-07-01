package com.example.solidconnection.mentor.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.mentor.dto.MentoringResponse;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MentoringQueryService {

    private final MentoringRepository mentoringRepository;
    private final MentorRepository mentorRepository;

    @Transactional(readOnly = true)
    public List<MentoringResponse> getMentorings(Long siteUserId) {
        Mentor mentor = mentorRepository.findBySiteUserId(siteUserId)
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));

        List<Mentoring> mentorings = mentoringRepository.findAllByMentorId(mentor.getId());

        return mentorings.stream()
                .map(MentoringResponse::from)
                .collect(Collectors.toList());
    }
}
