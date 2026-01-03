package com.example.solidconnection.admin.service;

import com.example.solidconnection.admin.dto.UserBanRequest;
import com.example.solidconnection.chat.repository.ChatMessageRepository;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.report.repository.ReportRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.domain.UserBan;
import com.example.solidconnection.siteuser.domain.UserStatus;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.siteuser.repository.UserBanRepository;
import static java.time.ZoneOffset.UTC;

import java.time.ZonedDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminUserBanService {

    private final UserBanRepository userBanRepository;
    private final ReportRepository reportRepository;
    private final SiteUserRepository siteUserRepository;
    private final PostRepository postRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public void banUser(long userId, UserBanRequest request) {
        ZonedDateTime now = ZonedDateTime.now(UTC);
        validateNotAlreadyBanned(userId, now);
        validateReportExists(userId);

        updateReportedContentIsDeleted(userId, true);
        createUserBan(userId, request, now);
        updateUserStatus(userId, UserStatus.BANNED);
    }

    private void validateNotAlreadyBanned(long userId, ZonedDateTime now) {
        if (userBanRepository.existsByBannedUserIdAndIsUnbannedFalseAndExpiredAtAfter(userId, now)) {
            throw new CustomException(ErrorCode.ALREADY_BANNED_USER);
        }
    }

    private void validateReportExists(long userId) {
        if (!reportRepository.existsByReportedId(userId)) {
            throw new CustomException(ErrorCode.REPORT_NOT_FOUND);
        }
    }

    private void updateReportedContentIsDeleted(long userId, boolean isDeleted) {
        postRepository.updateReportedPostsIsDeleted(userId, isDeleted);
        chatMessageRepository.updateReportedChatMessagesIsDeleted(userId, isDeleted);
    }

    private void createUserBan(long userId, UserBanRequest request, ZonedDateTime now) {
        ZonedDateTime expiredAt = now.plusDays(request.duration().getDays());
        UserBan userBan = new UserBan(userId, request.duration(), expiredAt);
        userBanRepository.save(userBan);
    }

    private void updateUserStatus(long userId, UserStatus status) {
        SiteUser user = siteUserRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateUserStatus(status);
    }

    @Transactional
    public void unbanUser(long userId, long adminId) {
        UserBan userBan = findBannedUser(userId, ZonedDateTime.now(UTC));
        userBan.manuallyUnban(adminId);
        processUnban(userId);
    }

    private UserBan findBannedUser(long userId, ZonedDateTime now) {
        return userBanRepository
                .findTopByBannedUserIdAndIsUnbannedFalseAndExpiredAtAfterOrderByCreatedAtDesc(userId, now)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_BANNED_USER));
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void expireUserBans() {
        List<UserBan> expiredBans = userBanRepository.findAllByIsUnbannedFalseAndExpiredAtBefore(ZonedDateTime.now(UTC));
        for (UserBan userBan : expiredBans) {
           processUnban(userBan.getBannedUserId());
        }
    }

    private void processUnban(long userId) {
        updateReportedContentIsDeleted(userId, false);
        updateUserStatus(userId, UserStatus.REPORTED);
    }
}
