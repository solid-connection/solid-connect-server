package com.example.solidconnection.application.repository;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.VerifyStatus;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
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
            JOIN FETCH a.siteUser
            WHERE (a.firstChoiceUniversityApplyInfoId IN :universityIds
                OR a.secondChoiceUniversityApplyInfoId IN :universityIds
                OR a.thirdChoiceUniversityApplyInfoId IN :universityIds)
                AND a.verifyStatus = :status
                AND a.term = :term
                AND a.isDelete = false
            """)
    List<Application> findApplicationsByUniversityChoices(@Param("universityIds") List<Long> universityIds, @Param("status") VerifyStatus status, @Param("term") String term);

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
