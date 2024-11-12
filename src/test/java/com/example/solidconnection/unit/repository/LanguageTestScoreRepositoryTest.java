package com.example.solidconnection.unit.repository;

import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("어학성적 레포지토리 테스트")
@Transactional
public class LanguageTestScoreRepositoryTest {
    @Autowired
    private SiteUserRepository siteUserRepository;
    @Autowired
    private LanguageTestScoreRepository languageTestScoreRepository;

    private SiteUser siteUser;

    @BeforeEach
    public void setUp() {
        siteUser = createSiteUser();
        siteUserRepository.save(siteUser);
    }

    private SiteUser createSiteUser() {
        return new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
    }

    @Test
    public void 사용자의_어학성적을_조회한다_기존이력_없을_때() {
        Optional<LanguageTestScore> languageTestScore = languageTestScoreRepository
                .findLanguageTestScoreBySiteUserAndLanguageTest_LanguageTestType(siteUser, LanguageTestType.TOEIC);
        assertThat(languageTestScore).isEqualTo(Optional.empty());
    }

    @Test
    public void 사용자의_어학성적을_조회한다_기존이력_있을_때() {
        LanguageTestScore languageTestScore = new LanguageTestScore(
                new LanguageTest(LanguageTestType.TOEIC, "990", "http://example.com/gpa-report.pdf"),
                LocalDate.of(2024, 10, 10),
                siteUser
        );
        languageTestScore.setSiteUser(siteUser);
        languageTestScoreRepository.save(languageTestScore);

        Optional<LanguageTestScore> languageTestScore1 = languageTestScoreRepository
                .findLanguageTestScoreBySiteUserAndLanguageTest_LanguageTestType(siteUser, LanguageTestType.TOEIC);
        assertThat(languageTestScore1).isEqualTo(Optional.of(languageTestScore));
    }

    @Test
    public void 아이디와_사용자정보로_사용자의_어학성적을_조회한다_기존이력_없을_때() {
        Optional<LanguageTestScore> languageTestScore = languageTestScoreRepository
                .findLanguageTestScoreBySiteUserAndId(siteUser, 1L);
        assertThat(languageTestScore).isEqualTo(Optional.empty());
    }

    @Test
    public void 아이디와_사용자정보로_사용자의_어학성적을_조회한다_기존이력_있을_때() {
        LanguageTestScore languageTestScore = new LanguageTestScore(
                new LanguageTest(LanguageTestType.TOEIC, "990", "http://example.com/gpa-report.pdf"),
                LocalDate.of(2024, 10, 10),
                siteUser
        );
        languageTestScore.setSiteUser(siteUser);
        languageTestScoreRepository.save(languageTestScore);

        Optional<LanguageTestScore> languageTestScore1 = languageTestScoreRepository
                .findLanguageTestScoreBySiteUserAndId(siteUser, languageTestScore.getId());
        assertThat(languageTestScore1).isEqualTo(Optional.of(languageTestScore));
    }
}
