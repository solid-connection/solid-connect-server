package com.example.solidconnection.siteuser.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.solidconnection.siteuser.domain.UserBan;

public interface UserBanRepository extends JpaRepository<UserBan, Long> {

    boolean existsByBannedUserIdAndIsUnbannedFalseAndExpiredAtAfter(long bannedUserId, ZonedDateTime current);

    List<UserBan> findAllByIsUnbannedFalseAndExpiredAtBefore(ZonedDateTime current);

    Optional<UserBan> findTopByBannedUserIdAndIsUnbannedFalseAndExpiredAtAfterOrderByCreatedAtDesc(long bannedUserId, ZonedDateTime current);
}
