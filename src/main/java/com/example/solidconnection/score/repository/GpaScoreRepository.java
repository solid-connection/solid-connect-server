package com.example.solidconnection.score.repository;

import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.repository.custom.GpaScoreFilterRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GpaScoreRepository extends JpaRepository<GpaScore, Long>, GpaScoreFilterRepository {

    Optional<GpaScore> findGpaScoreBySiteUser(SiteUser siteUser);

    Optional<GpaScore> findGpaScoreBySiteUserAndId(SiteUser siteUser, Long id);
}
