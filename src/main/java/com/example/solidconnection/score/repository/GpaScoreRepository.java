package com.example.solidconnection.score.repository;

import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.repository.custom.GpaScoreFilterRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GpaScoreRepository extends JpaRepository<GpaScore, Long>, GpaScoreFilterRepository {

    Optional<GpaScore> findGpaScoreBySiteUserId(long siteUserId);

    Optional<GpaScore> findGpaScoreBySiteUserIdAndId(long siteUserId, Long id);

    List<GpaScore> findBySiteUserId(long siteUserId);
}
