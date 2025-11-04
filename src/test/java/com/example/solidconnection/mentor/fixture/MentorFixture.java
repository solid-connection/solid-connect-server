package com.example.solidconnection.mentor.fixture;

import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.term.fixture.TermFixture;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class MentorFixture {

    private final MentorFixtureBuilder mentorFixtureBuilder;
    private final TermFixture termFixture;

    public Mentor 멘토(long siteUserId, long universityId) {
        return mentorFixtureBuilder.mentor()
                .siteUserId(siteUserId)
                .universityId(universityId)
                .introduction("멘토 소개")
                .passTip("합격 팁")
                .termId(termFixture.현재_학기("2025-1").getId())
                .create();
    }

    public Mentor 임시멘토(long siteUserId, long universityId) {
        return mentorFixtureBuilder.mentor()
                .siteUserId(siteUserId)
                .universityId(universityId)
                .introduction("멘토 소개")
                .passTip("합격 팁")
                .termId(termFixture.현재_학기("2025-1").getId())
                .createTempMentor();
    }
}
