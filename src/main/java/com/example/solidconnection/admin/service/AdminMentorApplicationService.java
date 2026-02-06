package com.example.solidconnection.admin.service;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_APPLICATION_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import com.example.solidconnection.admin.dto.MentorApplicationCountResponse;
import com.example.solidconnection.admin.dto.MentorApplicationHistoryResponse;
import com.example.solidconnection.admin.dto.MentorApplicationRejectRequest;
import com.example.solidconnection.admin.dto.MentorApplicationSearchCondition;
import com.example.solidconnection.admin.dto.MentorApplicationSearchResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.MentorApplication;
import com.example.solidconnection.mentor.domain.MentorApplicationStatus;
import com.example.solidconnection.mentor.repository.MentorApplicationRepository;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.repository.HostUniversityRepository;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminMentorApplicationService {

    private final MentorApplicationRepository mentorApplicationRepository;
    private final HostUniversityRepository hostUniversityRepository;
    private final SiteUserRepository siteUserRepository;
    private final MentorRepository mentorRepository;

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

        SiteUser siteUser = siteUserRepository.findById(mentorApplication.getSiteUserId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        siteUser.becomeMentor();

        Mentor mentor = new Mentor(
                "",
                "",
                siteUser.getId(),
                mentorApplication.getUniversityId(),
                mentorApplication.getTermId()
        );

        mentorRepository.save(mentor);
    }

    @Transactional
    public void rejectMentorApplication(
            Long mentorApplicationId,
            MentorApplicationRejectRequest request
    ) {
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

    @Transactional
    public void assignUniversity(
            Long mentorApplicationId,
            Long universityId
    ) {
        MentorApplication mentorApplication = mentorApplicationRepository.findById(mentorApplicationId)
                .orElseThrow(() -> new CustomException(MENTOR_APPLICATION_NOT_FOUND));

        mentorApplication.validateCanAssignUniversity();

        HostUniversity university = hostUniversityRepository.getHostUniversityById(universityId);

        mentorApplication.assignUniversity(university.getId());
    }

    @Transactional(readOnly = true)
    public List<MentorApplicationHistoryResponse> findMentorApplicationHistory(Long siteUserId) {
        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        long totalCount = mentorApplicationRepository.countBySiteUserId(siteUser.getId());
        List<MentorApplication> mentorApplications = mentorApplicationRepository.findTop5BySiteUserIdOrderByCreatedAtDesc(siteUser.getId());

        return IntStream.range(0, mentorApplications.size())
                .mapToObj(index -> {
                    MentorApplication app = mentorApplications.get(index);
                    return new MentorApplicationHistoryResponse(
                            app.getId(),
                            app.getMentorApplicationStatus(),
                            app.getRejectedReason(),
                            app.getCreatedAt(),
                            (int) totalCount - index
                    );
                }).toList();
    }
}
