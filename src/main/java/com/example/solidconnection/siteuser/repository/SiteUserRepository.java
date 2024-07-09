package com.example.solidconnection.siteuser.repository;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.example.solidconnection.custom.exception.ErrorCode.USER_NOT_FOUND;

@Repository
public interface SiteUserRepository extends JpaRepository<SiteUser, Long> {

    Optional<SiteUser> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    @Query("SELECT u FROM SiteUser u WHERE u.quitedAt <= :cutoffDate")
    List<SiteUser> findUsersToBeRemoved(@Param("cutoffDate") LocalDate cutoffDate);

    default SiteUser getByEmail(String email) {
        return findByEmail(email)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }
}
