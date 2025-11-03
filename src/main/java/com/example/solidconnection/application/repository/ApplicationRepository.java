package com.example.solidconnection.application.repository;

import static com.example.solidconnection.common.exception.ErrorCode.APPLICATION_NOT_FOUND;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.common.exception.CustomException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByNicknameForApply(String nicknameForApply);

    @Query("""
           SELECT a
           FROM Application a
           WHERE (a.firstChoiceUnivApplyInfoId IN :univApplyInfoIds
               OR a.secondChoiceUnivApplyInfoId IN :univApplyInfoIds
               OR a.thirdChoiceUnivApplyInfoId IN :univApplyInfoIds)
               AND a.verifyStatus = :status
               AND a.termId = :termId
               AND a.isDelete = false
           """)
    List<Application> findAllByUnivApplyInfoIds(@Param("univApplyInfoIds") List<Long> univApplyInfoIds, @Param("status") VerifyStatus status, @Param("termId") long termId);

    @Query("""
           SELECT a
           FROM Application a
           WHERE a.siteUserId = :siteUserId
               AND a.termId = :termId
               AND a.isDelete = false
           """)
    Optional<Application> findBySiteUserIdAndTermId(@Param("siteUserId") long siteUserId, @Param("termId") long termId);

    default Application getApplicationBySiteUserIdAndTermId(long siteUserId, long termId) {
        return findBySiteUserIdAndTermId(siteUserId, termId)
                .orElseThrow(() -> new CustomException(APPLICATION_NOT_FOUND));
    }
}
