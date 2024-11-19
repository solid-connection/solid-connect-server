package com.example.solidconnection.score.domain;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.entity.common.BaseEntity;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.VerifyStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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

    private LocalDate issueDate;

    @Setter
    @Column(columnDefinition = "varchar(50) not null default 'PENDING'")
    @Enumerated(EnumType.STRING)
    private VerifyStatus verifyStatus;

    private String rejectedReason;

    @OneToOne(fetch = FetchType.LAZY)
    private SiteUser siteUser;

    public GpaScore(Gpa gpa, SiteUser siteUser, LocalDate issueDate) {
        this.gpa = gpa;
        this.siteUser = siteUser;
        this.issueDate = issueDate;
        this.verifyStatus = VerifyStatus.PENDING;
        this.rejectedReason = null;
    }

    public void setSiteUser(SiteUser siteUser) {
        this.siteUser = siteUser;
        if (siteUser != null && siteUser.getGpaScore() != this) {
            siteUser.setGpaScore(this);
        }
    }

    public void update(Gpa gpa, LocalDate issueDate) {
        this.gpa = gpa;
        this.issueDate = issueDate;
        this.verifyStatus = VerifyStatus.PENDING;
        this.rejectedReason = null;
    }
}
