package com.example.solidconnection.score.domain;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.common.BaseEntity;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@NoArgsConstructor
@EqualsAndHashCode
public class GpaScore extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Embedded
    private Gpa gpa;

    @Setter
    @ColumnDefault("'PENDING'")
    @Column(name = "verify_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private VerifyStatus verifyStatus = VerifyStatus.PENDING;

    @Column(name = "rejected_reason")
    private String rejectedReason;

    @Column(name = "site_user_id", nullable = false)
    private long siteUserId;

    public GpaScore(Gpa gpa, SiteUser siteUser) {
        this.gpa = gpa;
        this.siteUserId = siteUser.getId();
        this.verifyStatus = VerifyStatus.PENDING;
        this.rejectedReason = null;
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
