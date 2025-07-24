package com.example.solidconnection.mentor.fixture;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class MentoringFixtureBuilder {

    private final MentoringRepository mentoringRepository;

    private ZonedDateTime createdAt;
    private ZonedDateTime confirmedAt;
    private ZonedDateTime checkedAt;
    private VerifyStatus verifyStatus = VerifyStatus.PENDING;
    private long mentorId;
    private long menteeId;

    public MentoringFixtureBuilder mentoring() {
        return new MentoringFixtureBuilder(mentoringRepository);
    }

    public MentoringFixtureBuilder createdAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public MentoringFixtureBuilder confirmedAt(ZonedDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
        return this;
    }

    public MentoringFixtureBuilder checkedAt(ZonedDateTime checkedAt) {
        this.checkedAt = checkedAt;
        return this;
    }

    public MentoringFixtureBuilder verifyStatus(VerifyStatus verifyStatus) {
        this.verifyStatus = verifyStatus;
        return this;
    }

    public MentoringFixtureBuilder mentorId(long mentorId) {
        this.mentorId = mentorId;
        return this;
    }

    public MentoringFixtureBuilder menteeId(long menteeId) {
        this.menteeId = menteeId;
        return this;
    }

    public Mentoring create() {
        Mentoring mentoring = new Mentoring(
                null,
                createdAt,
                confirmedAt,
                checkedAt,
                verifyStatus,
                mentorId,
                menteeId
        );
        return mentoringRepository.save(mentoring);
    }
}
