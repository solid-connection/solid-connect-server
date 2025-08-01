package com.example.solidconnection.mentor.service;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_NOT_FOUND;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.mentor.dto.MentoringForMenteeResponse;
import com.example.solidconnection.mentor.dto.MentoringForMentorResponse;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    public SliceResponse<MentoringForMenteeResponse> getMentoringsForMentee(
            long siteUserId, VerifyStatus verifyStatus, Pageable pageable
    ) {
        Slice<Mentoring> mentoringSlice = mentoringRepository.findAllByMenteeIdAndVerifyStatus(siteUserId, verifyStatus, pageable);
        List<Mentoring> mentorings = mentoringSlice.toList();

        Map<Mentoring, SiteUser> mentoringToPartnerUser = mapMentoringToPartnerUserWithBatchQuery(
                mentorings, Mentoring::getMentorId
        );

        List<MentoringForMenteeResponse> content = new ArrayList<>();
        for (Entry<Mentoring, SiteUser> entry : mentoringToPartnerUser.entrySet()) {
            content.add(MentoringForMenteeResponse.of(entry.getKey(), entry.getValue()));
        }

        return SliceResponse.of(content, mentoringSlice);
    }

    @Transactional(readOnly = true)
    public SliceResponse<MentoringForMentorResponse> getMentoringsForMentor(long siteUserId, Pageable pageable) {
        Mentor mentor = mentorRepository.findBySiteUserId(siteUserId)
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));
        Slice<Mentoring> mentoringSlice = mentoringRepository.findAllByMentorId(mentor.getId(), pageable);

        Map<Mentoring, SiteUser> mentoringToPartnerUser = mapMentoringToPartnerUserWithBatchQuery(
                mentoringSlice.toList(),
                Mentoring::getMenteeId
        );

        List<MentoringForMentorResponse> content = new ArrayList<>();
        for (Entry<Mentoring, SiteUser> entry : mentoringToPartnerUser.entrySet()) {
            content.add(MentoringForMentorResponse.of(entry.getKey(), entry.getValue()));
        }

        return SliceResponse.of(content, mentoringSlice);
    }

    // N+1 을 해결하면서 멘토링 상대방의 정보를 조회
    private Map<Mentoring, SiteUser> mapMentoringToPartnerUserWithBatchQuery(
            List<Mentoring> mentorings, Function<Mentoring, Long> getPartnerId
    ) {
        List<Long> partnerUserId = mentorings.stream()
                .map(getPartnerId)
                .distinct()
                .toList();
        List<SiteUser> partnerUsers = siteUserRepository.findAllById(partnerUserId);
        Map<Long, SiteUser> partnerIdToPartnerUsermap = partnerUsers.stream()
                .collect(Collectors.toMap(SiteUser::getId, Function.identity()));

        return mentorings.stream().collect(Collectors.toMap(
                Function.identity(),
                mentoring -> partnerIdToPartnerUsermap.get(getPartnerId.apply(mentoring))
        ));
    }
}
