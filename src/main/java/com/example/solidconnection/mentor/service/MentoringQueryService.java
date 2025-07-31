package com.example.solidconnection.mentor.service;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_NOT_FOUND;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.mentor.dto.MentoringResponse;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentoringQueryService {

    private final MentoringRepository mentoringRepository;
    private final MentorRepository mentorRepository;
    private final SiteUserRepository siteUserRepository;

    @Transactional(readOnly = true)
    public SliceResponse<MentoringResponse> getMentoringsForMentee(
            long siteUserId, VerifyStatus verifyStatus, Pageable pageable
    ) {
        Slice<Mentoring> mentoringSlice = mentoringRepository.findAllByMenteeIdAndVerifyStatus(siteUserId, verifyStatus, pageable);

        List<MentoringResponse> content = buildMentoringResponsesWithBatchQuery(
                mentoringSlice.toList(),
                Mentoring::getMentorId
        );

        return SliceResponse.of(content, mentoringSlice);
    }

    @Transactional(readOnly = true)
    public SliceResponse<MentoringResponse> getMentoringsForMentor(long siteUserId, Pageable pageable) {
        Mentor mentor = mentorRepository.findBySiteUserId(siteUserId)
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));
        Slice<Mentoring> mentoringSlice = mentoringRepository.findAllByMentorId(mentor.getId(), pageable);

        List<MentoringResponse> content = buildMentoringResponsesWithBatchQuery(
                mentoringSlice.toList(),
                Mentoring::getMenteeId
        );

        return SliceResponse.of(content, mentoringSlice);
    }

    private List<MentoringResponse> buildMentoringResponsesWithBatchQuery( // N+1 을 해결하면서 멘토링 상대방의 정보를 조회
            List<Mentoring> mentorings, Function<Mentoring, Long> getPartnerId
    ) {
        List<Long> partnerUserId = mentorings.stream()
                .map(getPartnerId)
                .distinct()
                .toList();
        List<SiteUser> partnerUsers = siteUserRepository.findAllById(partnerUserId);
        Map<Long, SiteUser> partnerUserMap = partnerUsers.stream()
                .collect(Collectors.toMap(SiteUser::getId, Function.identity()));

        List<MentoringResponse> mentoringResponses = new ArrayList<>();
        for (Mentoring mentoring : mentorings) {
            long partnerId = getPartnerId.apply(mentoring);
            SiteUser partnerUser = partnerUserMap.get(partnerId);
            mentoringResponses.add(MentoringResponse.from(mentoring, partnerUser));
        }
        return mentoringResponses;
    }
}
