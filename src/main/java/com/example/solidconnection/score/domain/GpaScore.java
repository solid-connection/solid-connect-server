package com.example.solidconnection.score.domain;

import com.example.solidconnection.application.domain.Gpa;
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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor
@EqualsAndHashCode
public class GpaScore extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Gpa gpa;

    @Setter
    @Column(columnDefinition = "varchar(50) not null default 'PENDING'")
    @Enumerated(EnumType.STRING)
    private VerifyStatus verifyStatus;

    private String rejectedReason;

    @ManyToOne
    private SiteUser siteUser;

    public GpaScore(Gpa gpa, SiteUser siteUser) {
        this.gpa = gpa;
        this.siteUser = siteUser;
        this.verifyStatus = VerifyStatus.PENDING;
        this.rejectedReason = null;
    }

    public void setSiteUser(SiteUser siteUser) {
        if (this.siteUser != null) {
            this.siteUser.getGpaScoreList().remove(this);
        }
        this.siteUser = siteUser;
        siteUser.getGpaScoreList().add(this);
    }

    public void updateGpaScore(Gpa gpa, VerifyStatus verifyStatus, String rejectedReason) {
        this.gpa = gpa;
        this.verifyStatus = verifyStatus;
        this.rejectedReason = rejectedReason;
    }

    public void updateGpa(Gpa gpa) {
        this.gpa = gpa;
    }
}
