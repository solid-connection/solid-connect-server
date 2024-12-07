package com.example.solidconnection.application.repository;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.VerifyStatus;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.example.solidconnection.custom.exception.ErrorCode.APPLICATION_NOT_FOUND;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByNicknameForApply(String nicknameForApply);

    List<Application> findAllByFirstChoiceUniversityAndVerifyStatusAndTerm(
            UniversityInfoForApply firstChoiceUniversity, VerifyStatus verifyStatus, String term);

    List<Application> findAllBySecondChoiceUniversityAndVerifyStatusAndTerm(
            UniversityInfoForApply secondChoiceUniversity, VerifyStatus verifyStatus, String term);

    List<Application> findAllByThirdChoiceUniversityAndVerifyStatusAndTerm(
            UniversityInfoForApply thirdChoiceUniversity, VerifyStatus verifyStatus, String term);

    @Query("""
        SELECT a FROM Application a
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
