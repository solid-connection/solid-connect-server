package com.example.solidconnection.admin.service;

import com.example.solidconnection.admin.dto.MentorApplicationSearchCondition;
import com.example.solidconnection.admin.dto.MentorApplicationSearchResponse;
import com.example.solidconnection.mentor.repository.MentorApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminMentorApplicationService {

    private final MentorApplicationRepository mentorApplicationRepository;

    @Transactional(readOnly = true)
    public Page<MentorApplicationSearchResponse> searchMentorApplications(
            MentorApplicationSearchCondition mentorApplicationSearchCondition,
            Pageable pageable
    ) {
        return mentorApplicationRepository.searchMentorApplications(mentorApplicationSearchCondition, pageable);
    }
}