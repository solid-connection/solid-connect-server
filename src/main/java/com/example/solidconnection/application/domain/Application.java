package com.example.solidconnection.application.domain;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.VerifyStatus;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import static com.example.solidconnection.type.VerifyStatus.PENDING;

@Getter
@DynamicInsert
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Gpa gpa;

    @Embedded
    private LanguageTest languageTest;

    @ColumnDefault("'PENDING'")
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private VerifyStatus verifyStatus;

    @Column(length = 100)
    private String nicknameForApply;

    @ColumnDefault("0")
    @Column(nullable = false)
    private Integer updateCount;

    @ManyToOne
    private UniversityInfoForApply firstChoiceUniversity;

    @ManyToOne
    private UniversityInfoForApply secondChoiceUniversity;

    @ManyToOne
    private SiteUser siteUser;

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
        this.verifyStatus = PENDING;
    }

    public void updateUniversityChoice(
            UniversityInfoForApply firstChoiceUniversity,
            UniversityInfoForApply secondChoiceUniversity,
            String nicknameForApply) {
        if (this.firstChoiceUniversity != null) {
            this.updateCount++;
        }
        this.firstChoiceUniversity = firstChoiceUniversity;
        this.secondChoiceUniversity = secondChoiceUniversity;
        this.nicknameForApply = nicknameForApply;
    }
}
