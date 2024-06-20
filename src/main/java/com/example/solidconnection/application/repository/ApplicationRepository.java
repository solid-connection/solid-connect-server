package com.example.solidconnection.application.repository;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.Application;
import com.example.solidconnection.entity.UniversityInfoForApply;
import com.example.solidconnection.type.VerifyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.example.solidconnection.custom.exception.ErrorCode.APPLICATION_NOT_FOUND;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsBySiteUser_Email(String email);

    boolean existsByNicknameForApply(String nicknameForApply);

    Optional<Application> findBySiteUser_Email(String email);

    List<Application> findAllByFirstChoiceUniversityAndVerifyStatus(UniversityInfoForApply firstChoiceUniversity, VerifyStatus verifyStatus);

    List<Application> findAllBySecondChoiceUniversityAndVerifyStatus(UniversityInfoForApply secondChoiceUniversity, VerifyStatus verifyStatus);

    default Application getBySiteUser_Email(String email) {
        return findBySiteUser_Email(email)
                .orElseThrow(() -> new CustomException(APPLICATION_NOT_FOUND));
    }
}
