package com.example.solidconnection.siteuser.service;

import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteUserService {
    private final SiteUserRepository siteUserRepository;

    public void deleteUsersNeverVisitedAfterQuited(int days) {
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        List<SiteUser> usersToRemove = siteUserRepository.findUsersToBeRemoved(cutoffDate);
        siteUserRepository.deleteAll(usersToRemove);
    }
}
