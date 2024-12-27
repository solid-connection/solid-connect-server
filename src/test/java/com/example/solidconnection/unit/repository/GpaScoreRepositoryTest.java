package com.example.solidconnection.unit.repository;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerDataJpaTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@TestContainerDataJpaTest
@DisplayName("학점 레포지토리 테스트")
@Transactional
public class GpaScoreRepositoryTest {
    @Autowired
    private SiteUserRepository siteUserRepository;
    @Autowired
    private GpaScoreRepository gpaScoreRepository;

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
    public void 사용자의_학점을_조회한다_기존이력_없을_때() {
        Optional<GpaScore> gpaScoreBySiteUser = gpaScoreRepository.findGpaScoreBySiteUser(siteUser);
        assertThat(gpaScoreBySiteUser).isEqualTo(Optional.empty());
    }

    @Test
    public void 사용자의_학점을_조회한다_기존이력_있을_때() {
        GpaScore gpaScore = new GpaScore(
                new Gpa(4.5, 4.5, "http://example.com/gpa-report.pdf"),
                siteUser,
                LocalDate.of(2024, 10, 10)
        );
        gpaScore.setSiteUser(siteUser);
        gpaScoreRepository.save(gpaScore);

        Optional<GpaScore> gpaScoreBySiteUser = gpaScoreRepository.findGpaScoreBySiteUser(siteUser);
        assertThat(gpaScoreBySiteUser).isEqualTo(Optional.of(gpaScore));
    }

    @Test
    public void 아이디와_사용자정보로_사용자의_학점을_조회한다_기존이력_없을_때() {
        Optional<GpaScore> gpaScoreBySiteUser = gpaScoreRepository.findGpaScoreBySiteUserAndId(siteUser, 1L);
        assertThat(gpaScoreBySiteUser).isEqualTo(Optional.empty());
    }

    @Test
    public void 아이디와_사용자정보로_사용자의_학점을_조회한다_기존이력_있을_때() {
        GpaScore gpaScore = new GpaScore(
                new Gpa(4.5, 4.5, "http://example.com/gpa-report.pdf"),
                siteUser,
                LocalDate.of(2024, 10, 10)
        );
        gpaScore.setSiteUser(siteUser);
        gpaScoreRepository.save(gpaScore);

        Optional<GpaScore> gpaScoreBySiteUser = gpaScoreRepository.findGpaScoreBySiteUserAndId(siteUser, gpaScore.getId());
        assertThat(gpaScoreBySiteUser).isEqualTo(Optional.of(gpaScore));
    }
}
