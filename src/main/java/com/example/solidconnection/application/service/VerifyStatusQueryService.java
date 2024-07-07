package com.example.solidconnection.application.service;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.dto.VerifyStatusResponse;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.VerifyStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.example.solidconnection.application.service.VerifyStatusQueryService.ApplicationStatusResponse.*;
import static com.example.solidconnection.application.service.VerifyStatusQueryService.ApplicationStatusResponse.SUBMITTED_APPROVED;
import static com.example.solidconnection.application.service.VerifyStatusQueryService.ApplicationStatusResponse.SUBMITTED_REJECTED;

@RequiredArgsConstructor
@Service
public class VerifyStatusQueryService {

    private final ApplicationRepository applicationRepository;
    private final SiteUserRepository siteUserRepository;

    public enum ApplicationStatusResponse {
        NOT_SUBMITTED, // 어떤 것도 제출하지 않음
        COLLEGE_SUBMITTED, // 지망 대학만 제출
        SCORE_SUBMITTED, // 성적만 제출
        SUBMITTED_PENDING, // 성적 인증 대기 중
        SUBMITTED_REJECTED, // 성적 인증 승인 완료
        SUBMITTED_APPROVED // 성적 인증 반려
    }

    /*
     * 지원 상태를 조회한다.
     * */
    public VerifyStatusResponse getVerifyStatus(String email) {
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        Optional<Application> application = applicationRepository.findBySiteUser_Email(siteUser.getEmail());

        // 아무것도 제출 안함
        if (application.isEmpty()) {
            return new VerifyStatusResponse(NOT_SUBMITTED.name(), 0);
        }

        int updateCount = application.get().getUpdateCount();
        // 제출한 상태
        if (application.get().getVerifyStatus() == VerifyStatus.PENDING) {
            // 지망 대학만 제출
            if (application.get().getGpa().getGpaReportUrl() == null) {
                return new VerifyStatusResponse(COLLEGE_SUBMITTED.name(), updateCount);
            }
            // 성적만 제출
            if (application.get().getFirstChoiceUniversity() == null) {
                return new VerifyStatusResponse(SCORE_SUBMITTED.name(), 0);
            }
            // 성적 승인 대기 중
            return new VerifyStatusResponse(SUBMITTED_PENDING.name(), updateCount);
        }
        // 성적 승인 반려
        if (application.get().getVerifyStatus() == VerifyStatus.REJECTED) {
            return new VerifyStatusResponse(SUBMITTED_REJECTED.name(), updateCount);
        }
        // 성적 승인 완료
        return new VerifyStatusResponse(SUBMITTED_APPROVED.name(), updateCount);
    }
}
