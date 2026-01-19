package com.example.solidconnection.score.repository;

import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.repository.custom.LanguageTestScoreFilterRepository;
import com.example.solidconnection.university.domain.LanguageTestType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageTestScoreRepository extends JpaRepository<LanguageTestScore, Long>, LanguageTestScoreFilterRepository {

    Optional<LanguageTestScore> findLanguageTestScoreBySiteUserIdAndLanguageTest_LanguageTestType(long siteUserId, LanguageTestType languageTestType);

    Optional<LanguageTestScore> findLanguageTestScoreBySiteUserIdAndId(long siteUserId, Long id);

    List<LanguageTestScore> findBySiteUserId(long siteUserId);

    void deleteAllBySiteUserId(long siteUserId);
}
