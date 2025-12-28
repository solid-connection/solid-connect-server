package com.example.solidconnection.admin.service;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.domain.UserStatus;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.solidconnection.admin.dto.UserBanRequest;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.report.repository.ReportRepository;
import com.example.solidconnection.siteuser.domain.UserBan;
import com.example.solidconnection.siteuser.repository.UserBanRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AdminUserBanService {

    private final UserBanRepository userBanRepository;
    private final ReportRepository reportRepository;
    private final SiteUserRepository siteUserRepository;

    @Transactional
    public void banUser(long userId, UserBanRequest request) {
        ZonedDateTime now = ZonedDateTime.now();
        validateNotAlreadyBanned(userId, now);
        validateReportExists(userId);

        deleteReportedContent();
        createUserBan(userId, request, now);
        updateUserStatus(userId, UserStatus.BANNED);
    }

    @Transactional
    public void unbanUser(long userId, long adminId) {
        ZonedDateTime now = ZonedDateTime.now();

        UserBan userBan = findBannedUser(userId, now);
        userBan.manuallyUnban(adminId);

        updateUserStatus(userId, UserStatus.REPORTED);
    }

    private void validateNotAlreadyBanned(long userId, ZonedDateTime now) {
        if (userBanRepository.existsByBannedUserIdAndIsUnbannedFalseAndExpiredAtAfter(userId, now)) {
            throw new CustomException(ErrorCode.ALREADY_BANNED_USER);
        }
    }

    private void validateReportExists(long userId) {
        reportRepository.findByTargetId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
    }

    private void deleteReportedContent() {
        //TODO report 타입에 따라서 콘텐츠 삭제 처리 로직 추가
    }

    private void createUserBan(long userId, UserBanRequest request, ZonedDateTime now) {
        ZonedDateTime expiredAt = now.plusDays(request.duration().getDays());
        UserBan userBan = new UserBan(userId, expiredAt);
        userBanRepository.save(userBan);
    }

    private UserBan findBannedUser(long userId, ZonedDateTime now) {
        return userBanRepository
                .findTopByBannedUserIdAndIsUnbannedFalseAndExpiredAtAfterOrderByCreatedAtDesc(userId, now)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_BANNED_USER));
    }

    private void updateUserStatus(long userId, UserStatus status) {
        SiteUser user = siteUserRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateUserStatus(status);
    }
}
