package com.example.solidconnection.admin.service;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_APPLICATION_NOT_FOUND;

import com.example.solidconnection.admin.dto.MentorApplicationCountResponse;
import com.example.solidconnection.admin.dto.MentorApplicationRejectRequest;
import com.example.solidconnection.admin.dto.MentorApplicationSearchCondition;
import com.example.solidconnection.admin.dto.MentorApplicationSearchResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.MentorApplication;
import com.example.solidconnection.mentor.domain.MentorApplicationStatus;
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


    @Transactional
    public void approveMentorApplication(Long mentorApplicationId) {
        MentorApplication mentorApplication = mentorApplicationRepository.findById(mentorApplicationId)
                .orElseThrow(() -> new CustomException(MENTOR_APPLICATION_NOT_FOUND));

        mentorApplication.approve();
    }

    @Transactional
    public void rejectMentorApplication(long mentorApplicationId, MentorApplicationRejectRequest request) {
        MentorApplication mentorApplication = mentorApplicationRepository.findById(mentorApplicationId)
                .orElseThrow(() -> new CustomException(MENTOR_APPLICATION_NOT_FOUND));

        mentorApplication.reject(request.rejectedReason());
    }

    @Transactional(readOnly = true)
    public MentorApplicationCountResponse getMentorApplicationCount() {
        long approvedCount = mentorApplicationRepository.countByMentorApplicationStatus(MentorApplicationStatus.APPROVED);
        long pendingCount = mentorApplicationRepository.countByMentorApplicationStatus(MentorApplicationStatus.PENDING);
        long rejectedCount = mentorApplicationRepository.countByMentorApplicationStatus(MentorApplicationStatus.REJECTED);

        return new MentorApplicationCountResponse(
                approvedCount,
                pendingCount,
                rejectedCount
        );
    }
}
