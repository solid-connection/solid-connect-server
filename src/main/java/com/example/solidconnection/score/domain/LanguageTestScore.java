package com.example.solidconnection.score.domain;

import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.common.BaseEntity;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Setter
    @Column(columnDefinition = "varchar(50) not null default 'PENDING'")
    @Enumerated(EnumType.STRING)
    private VerifyStatus verifyStatus;

    private String rejectedReason;

    @ManyToOne
    private SiteUser siteUser;

    public LanguageTestScore(LanguageTest languageTest, SiteUser siteUser) {
        this.languageTest = languageTest;
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

    public void updateLanguageTestScore(LanguageTest languageTest, VerifyStatus verifyStatus, String rejectedReason) {
        this.languageTest = languageTest;
        this.verifyStatus = verifyStatus;
        this.rejectedReason = rejectedReason;
    }
}
