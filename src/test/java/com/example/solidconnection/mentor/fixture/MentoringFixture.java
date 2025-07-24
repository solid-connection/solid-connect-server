package com.example.solidconnection.mentor.fixture;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MICROS;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.mentor.domain.Mentoring;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class MentoringFixture {

    private final MentoringFixtureBuilder mentoringFixtureBuilder;

    public Mentoring 대기중_멘토링(long mentorId, long menteeId) {
        return mentoringFixtureBuilder.mentoring()
                .mentorId(mentorId)
                .menteeId(menteeId)
                .create();
    }

    public Mentoring 승인된_멘토링(long mentorId, long menteeId) {
        ZonedDateTime now = getCurrentTime();
        return mentoringFixtureBuilder.mentoring()
                .mentorId(mentorId)
                .menteeId(menteeId)
                .verifyStatus(VerifyStatus.APPROVED)
                .confirmedAt(now)
                .checkedAt(now)
                .create();
    }

    public Mentoring 거절된_멘토링(long mentorId, long menteeId) {
        ZonedDateTime now = getCurrentTime();
        return mentoringFixtureBuilder.mentoring()
                .mentorId(mentorId)
                .menteeId(menteeId)
                .verifyStatus(VerifyStatus.REJECTED)
                .confirmedAt(now)
                .checkedAt(now)
                .create();
    }

    public Mentoring 확인되지_않은_멘토링(long mentorId, long menteeId) {
        return mentoringFixtureBuilder.mentoring()
                .mentorId(mentorId)
                .menteeId(menteeId)
                .checkedAt(null)
                .create();
    }

    private ZonedDateTime getCurrentTime() {
        return ZonedDateTime.now(UTC).truncatedTo(MICROS);
    }
}
