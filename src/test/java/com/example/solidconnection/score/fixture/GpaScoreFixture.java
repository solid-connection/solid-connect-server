package com.example.solidconnection.score.fixture;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class GpaScoreFixture {

    private final GpaScoreFixtureBuilder gpaScoreFixtureBuilder;

    public GpaScore GPA_점수 (VerifyStatus verifyStatus, SiteUser siteUser) {
        return gpaScoreFixtureBuilder.gpaScore()
                .gpa(new Gpa(4.0, 4.5, "/gpa-report.pdf"))
                .verifyStatus(verifyStatus)
                .siteUser(siteUser)
                .create();
    }
}
