package com.example.solidconnection.mentor.service;

import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.dto.MentorDetailResponse;
import com.example.solidconnection.mentor.dto.MentorPreviewResponse;
import com.example.solidconnection.mentor.repository.MentorBatchQueryRepository;
import com.example.solidconnection.mentor.dto.MentorPreviewResponse;
import com.example.solidconnection.mentor.dto.MentorPreviewsResponse;
import com.example.solidconnection.mentor.repository.MentorBatchQueryRepository;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class MentorQueryService {

    private final MentorRepository mentorRepository;
    private final MentoringRepository mentoringRepository;
    private final SiteUserRepository siteUserRepository;
    private final MentorBatchQueryRepository mentorBatchQueryRepository;

    @Transactional(readOnly = true)
    public MentorDetailResponse getMentorDetails(long mentorId, SiteUser currentUser) {
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));
        SiteUser mentorUser = siteUserRepository.findById(mentor.getSiteUserId())
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));
        boolean isApplied = mentoringRepository.existsByMentorIdAndMenteeId(mentorId, currentUser.getId());

        return MentorDetailResponse.of(mentor, mentorUser, isApplied);
    }

    @Transactional(readOnly = true)
    public SliceResponse<MentorPreviewResponse> getMentorPreviews(String region, SiteUser siteUser, Pageable pageable) { // todo: 멘토의 '인증' 작업 후 region 필터링 추가
        Slice<Mentor> mentorSlice = mentorRepository.findAllBy(pageable);
        List<Mentor> mentors = mentorSlice.toList();
        List<MentorPreviewResponse> content = getMentorPreviewResponses(mentors, siteUser);

        return SliceResponse.of(content, mentorSlice);
    }

    private List<MentorPreviewResponse> getMentorPreviewResponses(List<Mentor> mentors, SiteUser siteUser) {
        Map<Long, SiteUser> mentorIdToSiteUser = mentorBatchQueryRepository.getMentorIdToSiteUserMap(mentors);
        Map<Long, Boolean> mentorIdToIsApplied = mentorBatchQueryRepository.getMentorIdToIsApplied(mentors, siteUser.getId());

        List<MentorPreviewResponse> mentorPreviews = new ArrayList<>();
        for (Mentor mentor : mentors) {
            SiteUser mentorUser = mentorIdToSiteUser.get(mentor.getId());
            boolean isApplied = mentorIdToIsApplied.get(mentor.getId());
            MentorPreviewResponse response = MentorPreviewResponse.of(mentor, mentorUser, isApplied);
            mentorPreviews.add(response);
        }
        return mentorPreviews;
    }
}
