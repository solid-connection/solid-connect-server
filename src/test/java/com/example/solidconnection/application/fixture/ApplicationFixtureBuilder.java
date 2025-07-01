package com.example.solidconnection.application.fixture;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ApplicationFixtureBuilder {

    private final ApplicationRepository applicationRepository;

    private Gpa gpa;
    private LanguageTest languageTest;
    private Long firstChoiceUnivApplyInfoId;
    private Long secondChoiceUnivApplyInfoId;
    private Long thirdChoiceUnivApplyInfoId;
    private SiteUser siteUser;
    private String nicknameForApply;
    private String term;

    public ApplicationFixtureBuilder application() {
        return new ApplicationFixtureBuilder(applicationRepository);
    }

    public ApplicationFixtureBuilder gpa(Gpa gpa) {
        this.gpa = gpa;
        return this;
    }

    public ApplicationFixtureBuilder languageTest(LanguageTest languageTest) {
        this.languageTest = languageTest;
        return this;
    }

    public ApplicationFixtureBuilder firstChoiceUnivApplyInfoId(Long firstChoiceUnivApplyInfoId) {
        this.firstChoiceUnivApplyInfoId = firstChoiceUnivApplyInfoId;
        return this;
    }

    public ApplicationFixtureBuilder secondChoiceUnivApplyInfoId(Long secondChoiceUnivApplyInfoId) {
        this.secondChoiceUnivApplyInfoId = secondChoiceUnivApplyInfoId;
        return this;
    }

    public ApplicationFixtureBuilder thirdChoiceUnivApplyInfoId(Long thirdChoiceUnivApplyInfoId) {
        this.thirdChoiceUnivApplyInfoId = thirdChoiceUnivApplyInfoId;
        return this;
    }

    public ApplicationFixtureBuilder siteUser(SiteUser siteUser) {
        this.siteUser = siteUser;
        return this;
    }

    public ApplicationFixtureBuilder nicknameForApply(String nicknameForApply) {
        this.nicknameForApply = nicknameForApply;
        return this;
    }

    public ApplicationFixtureBuilder term(String term) {
        this.term = term;
        return this;
    }

    public Application create() {
        Application application = new Application(
                siteUser,
                gpa,
                languageTest,
                term,
                firstChoiceUnivApplyInfoId,
                secondChoiceUnivApplyInfoId,
                thirdChoiceUnivApplyInfoId,
                nicknameForApply
        );
        application.setVerifyStatus(VerifyStatus.APPROVED);
        return applicationRepository.save(application);
    }
}
