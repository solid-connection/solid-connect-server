package com.example.solidconnection.application.fixture;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.application.domain.VerifyStatus;
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
    private Long firstChoiceUniversityApplyInfoId;
    private Long secondChoiceUniversityApplyInfoId;
    private Long thirdChoiceUniversityApplyInfoId;
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

    public ApplicationFixtureBuilder firstChoiceUniversityApplyInfoId(Long firstChoiceUniversityApplyInfoId) {
        this.firstChoiceUniversityApplyInfoId = firstChoiceUniversityApplyInfoId;
        return this;
    }

    public ApplicationFixtureBuilder secondChoiceUniversityApplyInfoId(Long secondChoiceUniversityApplyInfoId) {
        this.secondChoiceUniversityApplyInfoId = secondChoiceUniversityApplyInfoId;
        return this;
    }

    public ApplicationFixtureBuilder thirdChoiceUniversityApplyInfoId(Long thirdChoiceUniversityApplyInfoId) {
        this.thirdChoiceUniversityApplyInfoId = thirdChoiceUniversityApplyInfoId;
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
                firstChoiceUniversityApplyInfoId,
                secondChoiceUniversityApplyInfoId,
                thirdChoiceUniversityApplyInfoId,
                nicknameForApply
        );
        application.setVerifyStatus(VerifyStatus.APPROVED);
        return applicationRepository.save(application);
    }
}
