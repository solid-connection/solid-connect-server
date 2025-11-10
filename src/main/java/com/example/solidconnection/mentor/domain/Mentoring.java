package com.example.solidconnection.mentor.domain;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MICROS;

import com.example.solidconnection.common.BaseEntity;
import com.example.solidconnection.common.VerifyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "mentoring", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_mentoring_mentor_id_mentee_id",
                columnNames = {"mentorId", "menteeId"}
        )
})
public class Mentoring extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private ZonedDateTime confirmedAt;

    @Column
    private ZonedDateTime checkedAtByMentor;

    @Column
    private ZonedDateTime checkedAtByMentee;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VerifyStatus verifyStatus = VerifyStatus.PENDING;

    @Column
    private long mentorId;

    @Column
    private long menteeId;

    public Mentoring(long mentorId, long menteeId, VerifyStatus verifyStatus) {
        this.mentorId = mentorId;
        this.menteeId = menteeId;
        this.verifyStatus = verifyStatus;
    }

    public void confirm(VerifyStatus status) {
        this.verifyStatus = status;
        this.confirmedAt = ZonedDateTime.now(UTC).truncatedTo(MICROS);

        if (this.checkedAtByMentor == null) {
            this.checkedAtByMentor = this.confirmedAt;
        }
        if (this.checkedAtByMentee != null) {
            this.checkedAtByMentee = null;
        }
    }

    public void checkByMentor() {
        this.checkedAtByMentor = ZonedDateTime.now(UTC).truncatedTo(MICROS);
    }

    public void checkByMentee() {
        this.checkedAtByMentee = ZonedDateTime.now(UTC).truncatedTo(MICROS);
    }
}
