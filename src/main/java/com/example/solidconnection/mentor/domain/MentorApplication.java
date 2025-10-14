package com.example.solidconnection.mentor.domain;

import com.example.solidconnection.common.BaseEntity;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.siteuser.domain.ExchangeStatus;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.persistence.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MentorApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long siteUserId;

    @Column(nullable = false, name = "country_code")
    private String countryCode;

    @Column
    private Long universityId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UniversitySelectType universitySelectType;

    @Column(nullable = false, name = "mentor_proof_url", length = 500)
    private String mentorProofUrl;

    private String rejectedReason;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExchangeStatus exchangeStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MentorApplicationStatus mentorApplicationStatus = MentorApplicationStatus.PENDING;

    private static final Set<ExchangeStatus> ALLOWED =
            Collections.unmodifiableSet(EnumSet.of(ExchangeStatus.STUDYING_ABROAD, ExchangeStatus.AFTER_EXCHANGE));

    public MentorApplication(
            SiteUser siteUser,
            String countryCode,
            Long universityId,
            UniversitySelectType universitySelectType,
            String mentorProofUrl,
            ExchangeStatus exchangeStatus
    ) {
        validateExchangeStatus(exchangeStatus);
        validateUniversitySelection(universitySelectType, universityId);

        this.siteUserId = siteUser.getId();
        this.countryCode = countryCode;
        this.universityId = universityId;
        this.universitySelectType = universitySelectType;
        this.mentorProofUrl = mentorProofUrl;
        this.exchangeStatus = exchangeStatus;
    }

    private void validateUniversitySelection(UniversitySelectType universitySelectType, Long universityId) {
        switch (universitySelectType) {
            case CATALOG -> {
                if(universityId == null) {
                    throw new CustomException(ErrorCode.UNIVERSITY_ID_REQUIRED_FOR_CATALOG);
                }
            }
            case OTHER -> {
                if(universityId != null) {
                    throw new CustomException(ErrorCode.UNIVERSITY_ID_MUST_BE_NULL_FOR_OTHER);
                }
            }
            default ->  throw new CustomException(ErrorCode.INVALID_UNIVERSITY_SELECT_TYPE);
        }
    }

    private void validateExchangeStatus(ExchangeStatus exchangeStatus) {
        if(!ALLOWED.contains(exchangeStatus)) {
            throw new CustomException(ErrorCode.INVALID_EXCHANGE_STATUS_FOR_MENTOR);
        }
    }
}
