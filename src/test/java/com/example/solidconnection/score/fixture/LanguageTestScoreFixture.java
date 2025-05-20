package com.example.solidconnection.score.fixture;

import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.application.domain.VerifyStatus;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.LanguageTestType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class LanguageTestScoreFixture {

    private final LanguageTestScoreFixtureBuilder languageTestScoreFixtureBuilder;

    public LanguageTestScore 어학_점수 (
            LanguageTestType languageTestType,
            String languageTestScore,
            VerifyStatus verifyStatus,
            SiteUser siteUser) {
        return languageTestScoreFixtureBuilder.languageTestScore()
                .languageTest(new LanguageTest(languageTestType, languageTestScore, "/language-report.pdf"))
                .verifyStatus(verifyStatus)
                .siteUser(siteUser)
                .create();
    }
}
