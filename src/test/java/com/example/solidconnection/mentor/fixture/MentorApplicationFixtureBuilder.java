package com.example.solidconnection.mentor.fixture;

import com.example.solidconnection.mentor.domain.MentorApplication;
import com.example.solidconnection.mentor.domain.MentorApplicationStatus;
import com.example.solidconnection.mentor.domain.UniversitySelectType;
import com.example.solidconnection.mentor.repository.MentorApplicationRepository;
import com.example.solidconnection.siteuser.domain.ExchangeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.util.ReflectionTestUtils;

@TestComponent
@RequiredArgsConstructor
public class MentorApplicationFixtureBuilder {

    private final MentorApplicationRepository mentorApplicationRepository;

    private long siteUserId;
    private String countryCode = "US";
    private Long universityId = null;
    private UniversitySelectType universitySelectType = UniversitySelectType.OTHER;
    private String mentorProofUrl = "/mentor-proof.pdf";
    private long termId;
    private ExchangeStatus exchangeStatus = ExchangeStatus.AFTER_EXCHANGE;
    private MentorApplicationStatus mentorApplicationStatus = MentorApplicationStatus.PENDING;

    public MentorApplicationFixtureBuilder mentorApplication() {
        return new MentorApplicationFixtureBuilder(mentorApplicationRepository);
    }

    public MentorApplicationFixtureBuilder siteUserId(long siteUserId) {
        this.siteUserId = siteUserId;
        return this;
    }

    public MentorApplicationFixtureBuilder countryCode(String countryCode) {
        this.countryCode = countryCode;
        return this;
    }

    public MentorApplicationFixtureBuilder universityId(Long universityId) {
        this.universityId = universityId;
        return this;
    }

    public MentorApplicationFixtureBuilder universitySelectType(UniversitySelectType universitySelectType) {
        this.universitySelectType = universitySelectType;
        return this;
    }

    public MentorApplicationFixtureBuilder mentorProofUrl(String mentorProofUrl) {
        this.mentorProofUrl = mentorProofUrl;
        return this;
    }

    public MentorApplicationFixtureBuilder termId(long termId) {
        this.termId = termId;
        return this;
    }

    public MentorApplicationFixtureBuilder exchangeStatus(ExchangeStatus exchangeStatus) {
        this.exchangeStatus = exchangeStatus;
        return this;
    }

    public MentorApplicationFixtureBuilder mentorApplicationStatus(MentorApplicationStatus mentorApplicationStatus) {
        this.mentorApplicationStatus = mentorApplicationStatus;
        return this;
    }

    public MentorApplication create() {
        MentorApplication mentorApplication = new MentorApplication(
                siteUserId,
                countryCode,
                universityId,
                universitySelectType,
                mentorProofUrl,
                termId,
                exchangeStatus
        );
        ReflectionTestUtils.setField(mentorApplication, "mentorApplicationStatus", mentorApplicationStatus);
        return mentorApplicationRepository.save(mentorApplication);
    }
}
