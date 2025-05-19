package com.example.solidconnection.university.repository;

import com.example.solidconnection.university.domain.LanguageRequirement;
import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LanguageRequirementRepository extends JpaRepository<LanguageRequirement, Long> {

    @Query("SELECT lr FROM LanguageRequirement lr WHERE lr.minScore <= :myScore AND lr.languageTestType = :testType AND lr.universityInfoForApply = :universityInfoForApply ORDER BY lr.minScore ASC")
    Optional<LanguageRequirement> findByUniversityInfoForApplyAndLanguageTestTypeAndLessThanMyScore(@Param("universityInfoForApply") UniversityInfoForApply universityInfoForApply, @Param("testType") LanguageTestType testType, @Param("myScore") String myScore);
}
