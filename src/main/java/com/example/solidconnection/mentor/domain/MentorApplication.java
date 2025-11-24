package com.example.solidconnection.mentor.domain;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_APPLICATION_ALREADY_CONFIRM;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MICROS;

import com.example.solidconnection.common.BaseEntity;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.siteuser.domain.ExchangeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Check(
        name = "chk_ma_university_select_rule",
        constraints = """ 
                      (university_select_type = 'CATALOG' AND university_id IS NOT NULL) OR
                      (university_select_type = 'OTHER' AND university_id IS NULL)
                      """
)
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

    @Column(nullable = false, name = "term_id")
    private long termId;

    private String rejectedReason;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExchangeStatus exchangeStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MentorApplicationStatus mentorApplicationStatus;

    @Column
    private ZonedDateTime approvedAt;

    private static final Set<ExchangeStatus> ALLOWED =
            Collections.unmodifiableSet(EnumSet.of(ExchangeStatus.STUDYING_ABROAD, ExchangeStatus.AFTER_EXCHANGE));

    public MentorApplication(
            long siteUserId,
            String countryCode,
            Long universityId,
            UniversitySelectType universitySelectType,
            String mentorProofUrl,
            long termId,
            ExchangeStatus exchangeStatus
    ) {
        validateExchangeStatus(exchangeStatus);
        validateUniversitySelection(universitySelectType, universityId);

        this.siteUserId = siteUserId;
        this.countryCode = countryCode;
        this.universityId = universityId;
        this.universitySelectType = universitySelectType;
        this.mentorProofUrl = mentorProofUrl;
        this.termId = termId;
        this.exchangeStatus = exchangeStatus;
        this.mentorApplicationStatus = MentorApplicationStatus.PENDING;
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

    public void approve(){
        if(this.mentorApplicationStatus != MentorApplicationStatus.PENDING) {
            throw new CustomException(MENTOR_APPLICATION_ALREADY_CONFIRM);
        }
        this.mentorApplicationStatus = MentorApplicationStatus.APPROVED;
        this.approvedAt = ZonedDateTime.now(UTC).truncatedTo(MICROS);
    }

    public void reject(String rejectedReason){
        if(this.mentorApplicationStatus != MentorApplicationStatus.PENDING) {
            throw new CustomException(MENTOR_APPLICATION_ALREADY_CONFIRM);
        }
        this.mentorApplicationStatus = MentorApplicationStatus.REJECTED;
        this.rejectedReason = rejectedReason;
    }
}
