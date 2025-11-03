package com.example.solidconnection.application.fixture;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ApplicationFixture {

    private final ApplicationFixtureBuilder applicationFixtureBuilder;

    public Application 지원서(
            SiteUser siteUser,
            String nicknameForApply,
            long termId,
            Gpa gpa,
            LanguageTest languageTest,
            Long firstChoiceUnivApplyInfoId,
            Long secondChoiceUnivApplyInfoId,
            Long thirdChoiceUnivApplyInfoId
    ) {
        return applicationFixtureBuilder.application()
                .siteUser(siteUser)
                .gpa(gpa)
                .languageTest(languageTest)
                .nicknameForApply(nicknameForApply)
                .termId(termId)
                .firstChoiceUnivApplyInfoId(firstChoiceUnivApplyInfoId)
                .secondChoiceUnivApplyInfoId(secondChoiceUnivApplyInfoId)
                .thirdChoiceUnivApplyInfoId(thirdChoiceUnivApplyInfoId)
                .create();
    }
}
