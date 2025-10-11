package com.example.solidconnection.mentor.domain;

import com.example.solidconnection.common.BaseEntity;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MentorApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private long siteUserId;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "region_code")
    private String regionCode;

    @Column
    private Long universityId;

    @Column(nullable = false, name = "mentor_proof_url", length = 500)
    private String mentorProofUrl;

    @Column
    private String rejectedReason;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExchangePhase exchangePhase;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MentorApplicationStatus mentorApplicationStatus = MentorApplicationStatus.PENDING;

    public MentorApplication(
            SiteUser siteUser,
            String countryCode,
            String regionCode,
            Long universityId,
            String mentorProofUrl,
            ExchangePhase exchangePhase
    ) {
        this.siteUserId = siteUser.getId();
        this.countryCode = countryCode;
        this.regionCode = regionCode;
        this.universityId = universityId;
        this.mentorProofUrl = mentorProofUrl;
        this.exchangePhase = exchangePhase;
    }
}
