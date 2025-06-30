package com.example.solidconnection.score.repository;

import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.repository.custom.LanguageTestScoreFilterRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.LanguageTestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LanguageTestScoreRepository extends JpaRepository<LanguageTestScore, Long>, LanguageTestScoreFilterRepository {

    Optional<LanguageTestScore> findLanguageTestScoreBySiteUserAndLanguageTest_LanguageTestType(SiteUser siteUser, LanguageTestType languageTestType);

    Optional<LanguageTestScore> findLanguageTestScoreBySiteUserAndId(SiteUser siteUser, Long id);
}
