package com.example.solidconnection.siteuser.repository;

import com.example.solidconnection.siteuser.domain.AuthType;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.domain.UserStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SiteUserRepository extends JpaRepository<SiteUser, Long> {

    Optional<SiteUser> findByEmailAndAuthType(String email, AuthType authType);

    boolean existsByEmailAndAuthType(String email, AuthType authType);

    boolean existsByNickname(String nickname);

    @Query("SELECT u FROM SiteUser u WHERE u.quitedAt <= :cutoffDate")
    List<SiteUser> findUsersToBeRemoved(@Param("cutoffDate") LocalDate cutoffDate);

    List<SiteUser> findAllByIdIn(List<Long> ids);

    @Modifying
    @Query("UPDATE SiteUser u SET u.userStatus = :status WHERE u.id IN :userIds")
    void bulkUpdateUserStatus(@Param("userIds") List<Long> userIds, @Param("status") UserStatus status);
}
