package com.example.solidconnection.score.repository;

import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.repository.custom.LanguageTestScoreFilterRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.LanguageTestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguageTestScoreRepository extends JpaRepository<LanguageTestScore, Long>, LanguageTestScoreFilterRepository {

    Optional<LanguageTestScore> findLanguageTestScoreBySiteUserIdAndLanguageTest_LanguageTestType(long siteUserId, LanguageTestType languageTestType);

    Optional<LanguageTestScore> findLanguageTestScoreBySiteUserIdAndId(long siteUser, Long id);
    List<LanguageTestScore> findBySiteUserId(Long siteUserId);
}
