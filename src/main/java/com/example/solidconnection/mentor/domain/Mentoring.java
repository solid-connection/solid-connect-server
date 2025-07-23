package com.example.solidconnection.mentor.domain;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MICROS;

import com.example.solidconnection.common.VerifyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mentoring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private ZonedDateTime createdAt;

    @Column
    private ZonedDateTime confirmedAt;

    @Column
    private ZonedDateTime checkedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VerifyStatus verifyStatus = VerifyStatus.PENDING;

    @Column(length = 500)
    private String rejectedReason;

    @Column
    private long mentorId;

    @Column
    private long menteeId;

    public Mentoring(long mentorId, long menteeId, VerifyStatus verifyStatus) {
        this.mentorId = mentorId;
        this.menteeId = menteeId;
        this.verifyStatus = verifyStatus;
    }

    @PrePersist
    public void onPrePersist() {
        this.createdAt = ZonedDateTime.now(UTC).truncatedTo(MICROS); // 나노초 6자리 까지만 저장
    }

    public void confirm(VerifyStatus status) {
        this.verifyStatus = status;
        this.confirmedAt = ZonedDateTime.now(UTC).truncatedTo(MICROS);

        if (this.checkedAt == null) {
            this.checkedAt = this.confirmedAt;
        }
    }

    public void check() {
        this.checkedAt = ZonedDateTime.now(UTC).truncatedTo(MICROS);
    }
}
