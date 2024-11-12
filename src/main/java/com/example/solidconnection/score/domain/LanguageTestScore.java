package com.example.solidconnection.score.domain;

import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.entity.common.BaseEntity;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.VerifyStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class LanguageTestScore extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private LanguageTest languageTest;

    private LocalDate issueDate;

    @Setter
    @Column(columnDefinition = "varchar(50) not null default 'PENDING'")
    @Enumerated(EnumType.STRING)
    private VerifyStatus verifyStatus;

    private String rejectedReason;

    @ManyToOne
    private SiteUser siteUser;

    public LanguageTestScore(LanguageTest languageTest, LocalDate issueDate, SiteUser siteUser) {
        this.languageTest = languageTest;
        this.issueDate = issueDate;
        this.verifyStatus = VerifyStatus.PENDING;
        this.siteUser = siteUser;
    }

    public void setSiteUser(SiteUser siteUser) {
        if (this.siteUser != null) {
            this.siteUser.getLanguageTestScoreList().remove(this);
        }
        this.siteUser = siteUser;
        siteUser.getLanguageTestScoreList().add(this);
    }

    public void update(LanguageTest languageTest, LocalDate issueDate) {
        this.languageTest = languageTest;
        this.issueDate = issueDate;
        this.verifyStatus = VerifyStatus.PENDING;
        this.rejectedReason = null;
    }
}
