package com.example.solidconnection.application.repository;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.VerifyStatus;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.example.solidconnection.common.exception.ErrorCode.APPLICATION_NOT_FOUND;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByNicknameForApply(String nicknameForApply);

    @Query("""
            SELECT a
            FROM Application a
            WHERE (a.firstChoiceUnivApplyInfoId IN :universityIds
                OR a.secondChoiceUnivApplyInfoId IN :universityIds
                OR a.thirdChoiceUnivApplyInfoId IN :universityIds)
                AND a.verifyStatus = :status
                AND a.term = :term
                AND a.isDelete = false
            """)
    List<Application> findAllByUnivApplyInfoIds(
            @Param("universityIds") List<Long> universityIds, @Param("status") VerifyStatus status, @Param("term") String term);

    @Query("""
            SELECT a
            FROM Application a
            WHERE a.siteUserId = :siteUserId
                AND a.term = :term
                AND a.isDelete = false
            """)
    Optional<Application> findBySiteUserIdAndTerm(@Param("siteUserId") Long siteUserId, @Param("term") String term);

    default Application getApplicationBySiteUserIdAndTerm(Long siteUserId, String term) {
        return findBySiteUserIdAndTerm(siteUserId, term)
                .orElseThrow(() -> new CustomException(APPLICATION_NOT_FOUND));
    }
}
