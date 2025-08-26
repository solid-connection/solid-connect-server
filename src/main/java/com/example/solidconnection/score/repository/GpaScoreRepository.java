package com.example.solidconnection.score.repository;

import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.repository.custom.GpaScoreFilterRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GpaScoreRepository extends JpaRepository<GpaScore, Long>, GpaScoreFilterRepository {

    Optional<GpaScore> findGpaScoreBySiteUserId(long siteUserId);

    Optional<GpaScore> findGpaScoreBySiteUserIdAndId(long siteUserId, Long id);

    List<GpaScore> findBySiteUserId(long siteUserId);
}
