package com.example.solidconnection.application.fixture;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.ApplicationChoice;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.siteuser.domain.SiteUser;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ApplicationFixtureBuilder {

    private final ApplicationRepository applicationRepository;

    private Gpa gpa;
    private LanguageTest languageTest;
    private List<Long> univApplyInfoIds = List.of();
    private SiteUser siteUser;
    private String nicknameForApply;
    private long termId;

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

    public ApplicationFixtureBuilder univApplyInfoIds(List<Long> univApplyInfoIds) {
        this.univApplyInfoIds = univApplyInfoIds;
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

    public ApplicationFixtureBuilder termId(long termId) {
        this.termId = termId;
        return this;
    }

    public Application create() {
        List<ApplicationChoice> choices = IntStream.range(0, univApplyInfoIds.size())
                .mapToObj(i -> new ApplicationChoice(i + 1, univApplyInfoIds.get(i)))
                .toList();
        Application application = new Application(
                siteUser,
                gpa,
                languageTest,
                termId,
                1,
                choices,
                nicknameForApply
        );
        application.setVerifyStatus(VerifyStatus.APPROVED);
        return applicationRepository.save(application);
    }
}
