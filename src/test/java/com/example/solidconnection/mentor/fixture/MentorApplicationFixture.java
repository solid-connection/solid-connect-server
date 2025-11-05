package com.example.solidconnection.mentor.fixture;

import com.example.solidconnection.mentor.domain.MentorApplication;
import com.example.solidconnection.mentor.domain.MentorApplicationStatus;
import com.example.solidconnection.mentor.domain.UniversitySelectType;
import com.example.solidconnection.siteuser.domain.ExchangeStatus;
import com.example.solidconnection.term.fixture.TermFixture;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class MentorApplicationFixture {

    private final MentorApplicationFixtureBuilder mentorApplicationFixtureBuilder;
    private final TermFixture termFixture;

    private static final String DEFAULT_COUNTRY_CODE = "US";
    private static final String DEFAULT_PROOF_URL   = "/mentor-proof.pdf";
    private static final ExchangeStatus DEFAULT_EXCHANGE_STATUS = ExchangeStatus.AFTER_EXCHANGE;

    public MentorApplication 대기중_멘토신청(
            long siteUserId,
            UniversitySelectType selectType,
            Long universityId
    ) {
        return mentorApplicationFixtureBuilder.mentorApplication()
                .siteUserId(siteUserId)
                .countryCode(DEFAULT_COUNTRY_CODE)
                .universityId(universityId)
                .universitySelectType(selectType)
                .mentorProofUrl(DEFAULT_PROOF_URL)
                .termId(termFixture.현재_학기("2025-1").getId())
                .exchangeStatus(DEFAULT_EXCHANGE_STATUS)
                .create();
    }

    public MentorApplication 승인된_멘토신청(
            long siteUserId,
            UniversitySelectType selectType,
            Long universityId
    ){
        return mentorApplicationFixtureBuilder.mentorApplication()
                .siteUserId(siteUserId)
                .countryCode(DEFAULT_COUNTRY_CODE)
                .universityId(universityId)
                .universitySelectType(selectType)
                .mentorProofUrl(DEFAULT_PROOF_URL)
                .exchangeStatus(DEFAULT_EXCHANGE_STATUS)
                .termId(termFixture.현재_학기("2025-1").getId())
                .mentorApplicationStatus(MentorApplicationStatus.APPROVED)
                .create();
    }

    public MentorApplication 거절된_멘토신청(
            long siteUserId,
            UniversitySelectType selectType,
            Long universityId
    ){
        return mentorApplicationFixtureBuilder.mentorApplication()
                .siteUserId(siteUserId)
                .countryCode(DEFAULT_COUNTRY_CODE)
                .universityId(universityId)
                .universitySelectType(selectType)
                .mentorProofUrl(DEFAULT_PROOF_URL)
                .exchangeStatus(DEFAULT_EXCHANGE_STATUS)
                .termId(termFixture.현재_학기("2025-1").getId())
                .mentorApplicationStatus(MentorApplicationStatus.REJECTED)
                .create();
    }
}