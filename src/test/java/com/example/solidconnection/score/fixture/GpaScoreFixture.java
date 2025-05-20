package com.example.solidconnection.score.fixture;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.VerifyStatus;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class GpaScoreFixture {

    private final GpaScoreFixtureBuilder gpaScoreFixtureBuilder;

    public GpaScore GPA점수(
            Double gpa,
            Double gpaCriteria,
            VerifyStatus verifyStatus,
            SiteUser siteUser) {
        return gpaScoreFixtureBuilder.gpaScore()
                .gpa(new Gpa(gpa, gpaCriteria, "/gpa-report.pdf"))
                .verifyStatus(verifyStatus)
                .siteUser(siteUser)
                .create();
    }
}
