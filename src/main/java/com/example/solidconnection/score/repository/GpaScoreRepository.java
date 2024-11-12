package com.example.solidconnection.score.repository;

import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.siteuser.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GpaScoreRepository extends JpaRepository<GpaScore, Long> {
    Optional<GpaScore> findGpaScoreBySiteUser(SiteUser siteUser);

    Optional<GpaScore> findGpaScoreBySiteUserAndId(SiteUser siteUser, Long id);
}
