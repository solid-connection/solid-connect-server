package com.example.solidconnection.application.domain;

import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.entity.UniversityInfoForApply;
import com.example.solidconnection.type.VerifyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import org.hibernate.annotations.DynamicInsert;

@Getter
@DynamicInsert
@Entity
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Gpa gpa;

    @Embedded
    private LanguageTest languageTest;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private VerifyStatus verifyStatus;

    @Column(length = 100)
    private String nicknameForApply;

    @Column(nullable = false)
    private Integer updateCount;

    // 연관 관계
    @ManyToOne
    @JoinColumn(name = "first_choice_univ_id")
    private UniversityInfoForApply firstChoiceUniversity;

    @ManyToOne
    @JoinColumn(name = "second_choice_univ_id")
    private UniversityInfoForApply secondChoiceUniversity;

    @ManyToOne
    @JoinColumn(name = "site_user_id")
    private SiteUser siteUser;

    protected Application() {}

    public Application(
            SiteUser siteUser,
            Gpa gpa,
            LanguageTest languageTest,
            String nicknameForApply) {
        this.siteUser = siteUser;
        this.gpa = gpa;
        this.languageTest = languageTest;
        this.nicknameForApply = nicknameForApply;
    }

    public Application(
            SiteUser siteUser,
            UniversityInfoForApply firstChoiceUniversity,
            UniversityInfoForApply secondChoiceUniversity,
            String nicknameForApply) {
        this.siteUser = siteUser;
        this.firstChoiceUniversity = firstChoiceUniversity;
        this.secondChoiceUniversity = secondChoiceUniversity;
        this.nicknameForApply = nicknameForApply;
    }

    public void updateGpaAndLanguageTest(
            Gpa gpa,
            LanguageTest languageTest) {
        this.gpa = gpa;
        this.languageTest = languageTest;
        this.verifyStatus = VerifyStatus.PENDING;
    }

    public void updateUniversityChoice(
            UniversityInfoForApply firstChoiceUniversity,
            UniversityInfoForApply secondChoiceUniversity,
            String nicknameForApply) {
        if(this.firstChoiceUniversity != null) {
            this.updateCount++;
        }
        this.firstChoiceUniversity = firstChoiceUniversity;
        this.secondChoiceUniversity = secondChoiceUniversity;
        this.nicknameForApply = nicknameForApply;
    }
}
