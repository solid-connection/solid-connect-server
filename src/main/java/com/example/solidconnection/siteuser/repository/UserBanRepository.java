package com.example.solidconnection.siteuser.repository;

import com.example.solidconnection.siteuser.domain.UserBan;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserBanRepository extends JpaRepository<UserBan, Long> {

    boolean existsByBannedUserIdAndIsExpiredFalseAndExpiredAtAfter(long bannedUserId, ZonedDateTime now);

    Optional<UserBan> findByBannedUserIdAndIsExpiredFalseAndExpiredAtAfter(long bannedUserId, ZonedDateTime now);

    @Query("SELECT ub.bannedUserId FROM UserBan ub WHERE ub.isExpired = false AND ub.expiredAt < :current")
    List<Long> findExpiredBannedUserIds(@Param("current") ZonedDateTime current);

    @Modifying
    @Query("UPDATE UserBan ub SET ub.isExpired = true WHERE ub.isExpired = false AND ub.expiredAt < :current")
    void bulkExpireUserBans(@Param("current") ZonedDateTime current);
}
