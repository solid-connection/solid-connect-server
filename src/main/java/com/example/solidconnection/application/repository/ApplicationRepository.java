package com.example.solidconnection.application.repository;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.VerifyStatus;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.example.solidconnection.custom.exception.ErrorCode.APPLICATION_NOT_FOUND;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByNicknameForApply(String nicknameForApply);

    Optional<Application> findBySiteUser_Email(String email);

    Optional<Application> findBySiteUser(SiteUser siteUser);

    List<Application> findAllByFirstChoiceUniversityAndVerifyStatus(UniversityInfoForApply firstChoiceUniversity, VerifyStatus verifyStatus);

    List<Application> findAllBySecondChoiceUniversityAndVerifyStatus(UniversityInfoForApply secondChoiceUniversity, VerifyStatus verifyStatus);

    List<Application> findAllByThirdChoiceUniversityAndVerifyStatus(UniversityInfoForApply thirdChoiceUniversity, VerifyStatus verifyStatus);

    default Application getApplicationBySiteUser(SiteUser siteUser) {
        return findBySiteUser(siteUser)
                .orElseThrow(() -> new CustomException(APPLICATION_NOT_FOUND));
    }
}
