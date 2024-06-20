package com.example.solidconnection.entity;

import com.example.solidconnection.application.dto.ScoreRequestDto;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.type.VerifyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private LanguageTestType languageTestType;

    @Column(nullable = false)
    private String languageTestScore;

    @Column(nullable = false, length = 500)
    private String languageTestReportUrl;

    @Column(nullable = false)
    private Float gpa;

    @Column(nullable = false)
    private Float gpaCriteria;

    @Column(nullable = false, length = 500)
    private String gpaReportUrl;

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

    private Application(SiteUser siteUser, String nicknameForApply) {
        this.siteUser = siteUser;
        this.nicknameForApply = nicknameForApply;
    }

    public static Application createWithScore(
            SiteUser siteUser,
            ScoreRequestDto scoreRequestDto,
            String nicknameForApply) {
        Application application = new Application(siteUser, nicknameForApply);
        application.updateWithScore(scoreRequestDto);
        return application;
    }

    public void updateWithScore(ScoreRequestDto scoreRequestDto) {
        this.languageTestType = scoreRequestDto.getLanguageTestType();
        this.languageTestScore = scoreRequestDto.getLanguageTestScore();
        this.languageTestReportUrl = scoreRequestDto.getLanguageTestReportUrl();
        this.gpa = scoreRequestDto.getGpa();
        this.gpaCriteria = scoreRequestDto.getGpaCriteria();
        this.gpaReportUrl = scoreRequestDto.getGpaReportUrl();
        this.verifyStatus = VerifyStatus.PENDING;
    }

    public static Application createWithUniversityChoice(
            SiteUser siteUser,
            UniversityInfoForApply firstChoiceUniversity,
            UniversityInfoForApply secondChoiceUniversity,
            String nicknameForApply) {
        Application application = new Application(siteUser, nicknameForApply);
        application.updateWithUniversityChoice(firstChoiceUniversity, secondChoiceUniversity, nicknameForApply);
        return application;
    }

    public void updateWithUniversityChoice(
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
