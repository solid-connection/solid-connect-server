package com.example.solidconnection.mentor.fixture;

import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.repository.MentorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class MentorFixtureBuilder {

    private final MentorRepository mentorRepository;

    private int menteeCount = 0;
    private boolean hasBadge = false;
    private String introduction;
    private String passTip;
    private long siteUserId;
    private long universityId;
    private long termId;

    public MentorFixtureBuilder mentor() {
        return new MentorFixtureBuilder(mentorRepository);
    }

    public MentorFixtureBuilder menteeCount(int menteeCount) {
        this.menteeCount = menteeCount;
        return this;
    }

    public MentorFixtureBuilder hasBadge(boolean hasBadge) {
        this.hasBadge = hasBadge;
        return this;
    }

    public MentorFixtureBuilder introduction(String introduction) {
        this.introduction = introduction;
        return this;
    }

    public MentorFixtureBuilder passTip(String passTip) {
        this.passTip = passTip;
        return this;
    }

    public MentorFixtureBuilder siteUserId(Long siteUserId) {
        this.siteUserId = siteUserId;
        return this;
    }

    public MentorFixtureBuilder universityId(Long universityId) {
        this.universityId = universityId;
        return this;
    }

    public MentorFixtureBuilder termId(long termId) {
        this.termId = termId;
        return this;
    }

    public Mentor create() {
        Mentor mentor = new Mentor(
                null,
                menteeCount,
                hasBadge,
                introduction,
                passTip,
                siteUserId,
                universityId,
                termId,
                null
        );
        return mentorRepository.save(mentor);
    }
}
