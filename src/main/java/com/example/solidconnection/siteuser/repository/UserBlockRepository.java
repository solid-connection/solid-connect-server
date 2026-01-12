package com.example.solidconnection.siteuser.repository;

import com.example.solidconnection.siteuser.domain.UserBlock;
import com.example.solidconnection.siteuser.dto.UserBlockResponse;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlockerIdAndBlockedId(long blockerId, long blockedId);

    Optional<UserBlock> findByBlockerIdAndBlockedId(long blockerId, long blockedId);

    @Query("""
           SELECT new com.example.solidconnection.siteuser.dto.UserBlockResponse(
               ub.id, ub.blockedId, su.nickname, ub.createdAt
           )
           FROM UserBlock ub
           JOIN SiteUser su ON ub.blockedId = su.id
           WHERE ub.blockerId = :blockerId
           """)
    Slice<UserBlockResponse> findBlockedUsersWithNickname(@Param("blockerId") long blockerId, Pageable pageable);

    void deleteAllByBlockerIdOrBlockedId(long blockerId, long blockedId);
}
