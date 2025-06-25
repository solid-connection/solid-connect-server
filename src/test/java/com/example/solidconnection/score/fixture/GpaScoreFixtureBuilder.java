package com.example.solidconnection.score.fixture;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class GpaScoreFixtureBuilder {

    private final GpaScoreRepository gpaScoreRepository;

    private Gpa gpa;
    private VerifyStatus verifyStatus;
    private SiteUser siteUser;

    public GpaScoreFixtureBuilder gpaScore() {
        return new GpaScoreFixtureBuilder(gpaScoreRepository);
    }

    public GpaScoreFixtureBuilder gpa(Gpa gpa) {
        this.gpa = gpa;
        return this;
    }

    public GpaScoreFixtureBuilder verifyStatus(VerifyStatus verifyStatus) {
        this.verifyStatus = verifyStatus;
        return this;
    }

    public GpaScoreFixtureBuilder siteUser(SiteUser siteUser) {
        this.siteUser = siteUser;
        return this;
    }

    public GpaScore create() {
        GpaScore gpaScore = new GpaScore(gpa, siteUser);
        gpaScore.setSiteUser(siteUser);
        gpaScore.setVerifyStatus(verifyStatus);
        return gpaScoreRepository.save(gpaScore);
    }
}
