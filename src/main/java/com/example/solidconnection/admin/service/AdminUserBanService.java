package com.example.solidconnection.admin.service;

import static java.time.ZoneOffset.UTC;

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
    public void banUser(long userId, long adminId, UserBanRequest request) {
        SiteUser user = siteUserRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        validateNotAlreadyBanned(userId);
        validateReportExists(userId);

        user.updateUserStatus(UserStatus.BANNED);
        updateReportedContentIsDeleted(userId, true);
        createUserBan(userId, adminId, request);
    }

    private void validateNotAlreadyBanned(long userId) {
        if (userBanRepository.existsByBannedUserIdAndIsExpiredFalseAndExpiredAtAfter(userId, ZonedDateTime.now(UTC))) {
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

    private void createUserBan(long userId, long adminId, UserBanRequest request) {
        ZonedDateTime now = ZonedDateTime.now(UTC);
        ZonedDateTime expiredAt = now.plusDays(request.duration().getDays());
        UserBan userBan = new UserBan(userId, adminId, request.duration(), expiredAt);
        userBanRepository.save(userBan);
    }

    @Transactional
    public void unbanUser(long userId, long adminId) {
        SiteUser user = siteUserRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        UserBan userBan = findActiveBan(userId);
        userBan.manuallyUnban(adminId);

        user.updateUserStatus(UserStatus.REPORTED);
        updateReportedContentIsDeleted(userId, false);
    }

    private UserBan findActiveBan(long userId) {
        return userBanRepository
                .findByBannedUserIdAndIsExpiredFalseAndExpiredAtAfter(userId, ZonedDateTime.now(UTC))
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_BANNED_USER));
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void expireUserBans() {
        try {
            ZonedDateTime now = ZonedDateTime.now(UTC);
            List<Long> expiredUserIds = userBanRepository.findExpiredBannedUserIds(now);

            if (expiredUserIds.isEmpty()) {
                return;
            }

            userBanRepository.bulkExpireUserBans(now);
            siteUserRepository.bulkUpdateUserStatus(expiredUserIds, UserStatus.REPORTED);
            bulkUpdateReportedContentIsDeleted(expiredUserIds);
            log.info("Finished processing expired blocks:: userIds={}", expiredUserIds);
        } catch (Exception e) {
            log.error("Failed to process expired blocks", e);
        }
    }

    private void bulkUpdateReportedContentIsDeleted(List<Long> expiredUserIds) {
        postRepository.bulkUpdateReportedPostsIsDeleted(expiredUserIds, false);
        chatMessageRepository.bulkUpdateReportedChatMessagesIsDeleted(expiredUserIds, false);
    }

}
