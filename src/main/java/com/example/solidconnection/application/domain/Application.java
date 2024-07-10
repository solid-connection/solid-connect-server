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
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static com.example.solidconnection.type.VerifyStatus.PENDING;

@ToString
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

    @Column(columnDefinition = "varchar(50) not null default 'PENDING'")
    @Enumerated(EnumType.STRING)
    private VerifyStatus verifyStatus;

    @Column(length = 100)
    private String nicknameForApply;

    @Column(columnDefinition = "int not null default 0")
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
            LanguageTest languageTest) {
        this.siteUser = siteUser;
        this.gpa = gpa;
        this.languageTest = languageTest;
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
