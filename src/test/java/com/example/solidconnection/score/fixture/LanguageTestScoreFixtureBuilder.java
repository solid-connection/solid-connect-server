package com.example.solidconnection.score.fixture;

import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.application.domain.VerifyStatus;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class LanguageTestScoreFixtureBuilder {

    private final LanguageTestScoreRepository languageTestScoreRepository;

    private LanguageTest languageTest;
    private VerifyStatus verifyStatus;
    private SiteUser siteUser;

    public LanguageTestScoreFixtureBuilder languageTestScore() {
        return new LanguageTestScoreFixtureBuilder(languageTestScoreRepository);
    }

    public LanguageTestScoreFixtureBuilder languageTest(LanguageTest languageTest) {
        this.languageTest = languageTest;
        return this;
    }

    public LanguageTestScoreFixtureBuilder verifyStatus(VerifyStatus verifyStatus) {
        this.verifyStatus = verifyStatus;
        return this;
    }

    public LanguageTestScoreFixtureBuilder siteUser(SiteUser siteUser) {
        this.siteUser = siteUser;
        return this;
    }

    public LanguageTestScore create() {
        LanguageTestScore languageTestScore = new LanguageTestScore(languageTest, siteUser);
        languageTestScore.setVerifyStatus(verifyStatus);
        return languageTestScoreRepository.save(languageTestScore);
    }
}
