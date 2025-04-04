package com.example.solidconnection.application.domain;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.VerifyStatus;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static com.example.solidconnection.type.VerifyStatus.PENDING;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@DynamicUpdate
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

    @Setter
    @Column(columnDefinition = "varchar(50) not null default 'PENDING'")
    @Enumerated(EnumType.STRING)
    private VerifyStatus verifyStatus;

    @Column(length = 100)
    private String nicknameForApply;

    @Column(columnDefinition = "int not null default 1")
    private Integer updateCount;

    @Column(length = 50, nullable = false)
    private String term;

    @Column
    private boolean isDelete = false;

    @ManyToOne(fetch = FetchType.LAZY)
    private UniversityInfoForApply firstChoiceUniversity;

    @ManyToOne(fetch = FetchType.LAZY)
    private UniversityInfoForApply secondChoiceUniversity;

    @ManyToOne(fetch = FetchType.LAZY)
    private UniversityInfoForApply thirdChoiceUniversity;

    @ManyToOne(fetch = FetchType.LAZY)
    private SiteUser siteUser;

    public Application(
            SiteUser siteUser,
            Gpa gpa,
            LanguageTest languageTest,
            String term) {
        this.siteUser = siteUser;
        this.gpa = gpa;
        this.languageTest = languageTest;
        this.term = term;
        this.updateCount = 1;
        this.verifyStatus = PENDING;
    }

    public Application(
            SiteUser siteUser,
            Gpa gpa,
            LanguageTest languageTest,
            String term,
            Integer updateCount,
            UniversityInfoForApply firstChoiceUniversity,
            UniversityInfoForApply secondChoiceUniversity,
            UniversityInfoForApply thirdChoiceUniversity,
            String nicknameForApply) {
        this.siteUser = siteUser;
        this.gpa = gpa;
        this.languageTest = languageTest;
        this.term = term;
        this.updateCount = updateCount;
        this.firstChoiceUniversity = firstChoiceUniversity;
        this.secondChoiceUniversity = secondChoiceUniversity;
        this.thirdChoiceUniversity = thirdChoiceUniversity;
        this.nicknameForApply = nicknameForApply;
        this.verifyStatus = PENDING;
    }

    public Application(
            SiteUser siteUser,
            Gpa gpa,
            LanguageTest languageTest,
            String term,
            UniversityInfoForApply firstChoiceUniversity,
            UniversityInfoForApply secondChoiceUniversity,
            UniversityInfoForApply thirdChoiceUniversity,
            String nicknameForApply) {
        this.siteUser = siteUser;
        this.gpa = gpa;
        this.languageTest = languageTest;
        this.term = term;
        this.updateCount = 1;
        this.firstChoiceUniversity = firstChoiceUniversity;
        this.secondChoiceUniversity = secondChoiceUniversity;
        this.thirdChoiceUniversity = thirdChoiceUniversity;
        this.nicknameForApply = nicknameForApply;
        this.verifyStatus = PENDING;
    }

    public void setIsDeleteTrue() {
        this.isDelete = true;
    }

    public void updateUniversityChoice(
            UniversityInfoForApply firstChoiceUniversity,
            UniversityInfoForApply secondChoiceUniversity,
            UniversityInfoForApply thirdChoiceUniversity,
            String nicknameForApply) {
        if (this.firstChoiceUniversity != null) {
            this.updateCount++;
        }
        this.firstChoiceUniversity = firstChoiceUniversity;
        this.secondChoiceUniversity = secondChoiceUniversity;
        this.thirdChoiceUniversity = thirdChoiceUniversity;
        this.nicknameForApply = nicknameForApply;
    }
}
