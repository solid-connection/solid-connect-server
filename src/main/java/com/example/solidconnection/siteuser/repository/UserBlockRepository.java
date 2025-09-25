package com.example.solidconnection.siteuser.repository;

import com.example.solidconnection.siteuser.domain.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlockerIdAndBlockedId(long blockerId, long blockedId);
}
