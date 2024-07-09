package com.example.solidconnection.scheduler;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Component
public class UserRemovalScheduler {

    public static final String EVERY_MIDNIGHT = "0 0 0 * * ?";
    public static final int ACCOUNT_RECOVER_DURATION = 30;

    private final SiteUserRepository siteUserRepository;

    /*
     * 탈퇴 후 계정 복구 기한까지 방문하지 않은 사용자를 삭제한다.
     * */
    @Scheduled(cron = EVERY_MIDNIGHT)
    public void scheduledUserRemoval() {
        LocalDate cutoffDate = LocalDate.now().minusDays(ACCOUNT_RECOVER_DURATION);
        List<SiteUser> usersToRemove = siteUserRepository.findUsersToBeRemoved(cutoffDate);
        siteUserRepository.deleteAll(usersToRemove);
    }
}
