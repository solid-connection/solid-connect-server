package com.example.solidconnection.application.repository;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import static com.example.solidconnection.common.exception.ErrorCode.APPLICATION_NOT_FOUND;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByNicknameForApply(String nicknameForApply);

    @Query("""
            SELECT a
            FROM Application a
            JOIN FETCH a.siteUser
            WHERE (a.firstChoiceUnivApplyInfoId IN :univApplyInfoIds
                OR a.secondChoiceUnivApplyInfoId IN :univApplyInfoIds
                OR a.thirdChoiceUnivApplyInfoId IN :univApplyInfoIds)
                AND a.verifyStatus = :status
                AND a.term = :term
                AND a.isDelete = false
            """)
    List<Application> findAllByUnivApplyInfoIds(@Param("univApplyInfoIds") List<Long> univApplyInfoIds, @Param("status") VerifyStatus status, @Param("term") String term);

    @Query("""
            SELECT a
            FROM Application a
            WHERE a.siteUser = :siteUser
                AND a.term = :term
                AND a.isDelete = false
            """)
    Optional<Application> findBySiteUserAndTerm(@Param("siteUser") SiteUser siteUser, @Param("term") String term);

    default Application getApplicationBySiteUserAndTerm(SiteUser siteUser, String term) {
        return findBySiteUserAndTerm(siteUser, term)
                .orElseThrow(() -> new CustomException(APPLICATION_NOT_FOUND));
    }
}
