package com.example.solidconnection.application.fixture;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ApplicationFixture {

    private final ApplicationFixtureBuilder applicationFixtureBuilder;

    public Application 지원서(
            SiteUser siteUser,
            String nicknameForApply,
            String term,
            Gpa gpa,
            LanguageTest languageTest,
            UniversityInfoForApply firstChoiceUniversity,
            UniversityInfoForApply secondChoiceUniversity,
            UniversityInfoForApply thirdChoiceUniversity
    ) {
        return applicationFixtureBuilder.application()
                .siteUser(siteUser)
                .gpa(gpa)
                .languageTest(languageTest)
                .nicknameForApply(nicknameForApply)
                .term(term)
                .firstChoiceUniversity(firstChoiceUniversity)
                .secondChoiceUniversity(secondChoiceUniversity)
                .thirdChoiceUniversity(thirdChoiceUniversity)
                .create();
    }
}
