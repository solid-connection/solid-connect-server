package com.example.solidconnection.admin.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.domain.UserBan;
import com.example.solidconnection.siteuser.domain.UserStatus;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.siteuser.repository.UserBanRepository;
import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserBanScheduler {

    private final UserBanRepository userBanRepository;
    private final SiteUserRepository siteUserRepository;

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void expireUserBans() {
        ZonedDateTime current = ZonedDateTime.now();
        List<UserBan> expiredBans = userBanRepository.findAllByIsUnbannedFalseAndExpiredAtBefore(current);

        for (UserBan userBan : expiredBans) {
            updateUserStatus(userBan);
        }
    }

    private void updateUserStatus(UserBan userBan) {
        SiteUser user = siteUserRepository.findById(userBan.getBannedUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateUserStatus(UserStatus.REPORTED); // 다시 신고됨 상태로 변경
    }
}
