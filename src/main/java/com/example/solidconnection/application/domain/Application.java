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

    @Column(columnDefinition = "int not null default 0")
    private Integer updateCount;

    @Column(length = 50, nullable = false)
    private String term;

    @ManyToOne
    private UniversityInfoForApply firstChoiceUniversity;

    @ManyToOne
    private UniversityInfoForApply secondChoiceUniversity;

    @ManyToOne
    private UniversityInfoForApply thirdChoiceUniversity;

    @ManyToOne
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
        this.updateCount = 0;
        this.verifyStatus = PENDING;
    }

    public void updateApplication(
            Gpa gpa,
            LanguageTest languageTest,
            UniversityInfoForApply firstChoiceUniversity,
            UniversityInfoForApply secondChoiceUniversity,
            UniversityInfoForApply thirdChoiceUniversity,
            String nicknameForApply
    ) {
        this.gpa = gpa;
        this.languageTest = languageTest;
        if (this.firstChoiceUniversity != null) {
            this.updateCount++;
        }
        this.firstChoiceUniversity = firstChoiceUniversity;
        this.secondChoiceUniversity = secondChoiceUniversity;
        this.thirdChoiceUniversity = thirdChoiceUniversity;
        this.nicknameForApply = nicknameForApply;
        this.verifyStatus = VerifyStatus.APPROVED;
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
