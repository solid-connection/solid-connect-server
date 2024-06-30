package com.example.solidconnection.scheduler;

import com.example.solidconnection.siteuser.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserRemovalScheduler {

    public static final String EVERY_MIDNIGHT = "0 0 0 * * ?";
    public static final int ACCOUNT_RECOVER_DURATION = 30;

    private final SiteUserService siteUserService;

    @Scheduled(cron = EVERY_MIDNIGHT)
    public void scheduledUserRemoval() {
        siteUserService.deleteUsersNeverVisitedAfterQuited(ACCOUNT_RECOVER_DURATION);
    }
}
