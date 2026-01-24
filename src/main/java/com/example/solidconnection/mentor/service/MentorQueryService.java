package com.example.solidconnection.mentor.service;

import static com.example.solidconnection.common.exception.ErrorCode.MENTOR_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.TERM_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.UNIVERSITY_NOT_FOUND;

import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.location.region.domain.Region;
import com.example.solidconnection.location.region.repository.RegionRepository;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.dto.MentorDetailResponse;
import com.example.solidconnection.mentor.dto.MentorPreviewResponse;
import com.example.solidconnection.mentor.repository.MentorBatchQueryRepository;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.term.domain.Term;
import com.example.solidconnection.term.repository.TermRepository;
import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.repository.HostUniversityRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MentorQueryService {

    private final MentorRepository mentorRepository;
    private final MentoringRepository mentoringRepository;
    private final SiteUserRepository siteUserRepository;
    private final MentorBatchQueryRepository mentorBatchQueryRepository;
    private final HostUniversityRepository hostUniversityRepository;
    private final RegionRepository regionRepository;
    private final TermRepository termRepository;

    @Transactional(readOnly = true)
    public MentorDetailResponse getMentorDetails(long mentorId, long currentUserId) {
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));
        HostUniversity university = hostUniversityRepository.findById(mentor.getUniversityId())
                .orElseThrow(() -> new CustomException(UNIVERSITY_NOT_FOUND));
        SiteUser mentorUser = siteUserRepository.findById(mentor.getSiteUserId())
                .orElseThrow(() -> new CustomException(MENTOR_NOT_FOUND));
        Term term = termRepository.findById(mentor.getTermId())
                .orElseThrow(() -> new CustomException(TERM_NOT_FOUND));
        boolean isApplied = mentoringRepository.existsByMentorIdAndMenteeId(mentorId, currentUserId);

        return MentorDetailResponse.of(mentor, mentorUser, university, isApplied, term.getName());
    }

    @Transactional(readOnly = true)
    public SliceResponse<MentorPreviewResponse> getMentorPreviews(String regionKoreanName, long currentUserId, Pageable pageable) {
        Slice<Mentor> mentorSlice = filterMentorsByRegion(regionKoreanName, pageable);
        List<Mentor> mentors = mentorSlice.toList();
        List<MentorPreviewResponse> content = buildMentorPreviewsWithBatchQuery(mentors, currentUserId);

        return SliceResponse.of(content, mentorSlice);
    }

    private Slice<Mentor> filterMentorsByRegion(String regionKoreanName, Pageable pageable) {
        if (regionKoreanName == null || regionKoreanName.isEmpty()) {
            return mentorRepository.findAll(pageable);
        }
        Region region = regionRepository.findByKoreanName(regionKoreanName)
                .orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND_BY_KOREAN_NAME));
        return mentorRepository.findAllByRegion(region, pageable);
    }

    private List<MentorPreviewResponse> buildMentorPreviewsWithBatchQuery(List<Mentor> mentors, long currentUserId) {
        Map<Long, SiteUser> mentorIdToSiteUser = mentorBatchQueryRepository.getMentorIdToSiteUserMap(mentors);
        Map<Long, HostUniversity> mentorIdToUniversity = mentorBatchQueryRepository.getMentorIdToUniversityMap(mentors);
        Map<Long, Boolean> mentorIdToIsApplied = mentorBatchQueryRepository.getMentorIdToIsApplied(mentors, currentUserId);
        Map<Long, String> termIdToName = mentorBatchQueryRepository.getTermIdToNameMap(mentors);

        List<MentorPreviewResponse> mentorPreviews = new ArrayList<>();
        for (Mentor mentor : mentors) {
            SiteUser mentorUser = mentorIdToSiteUser.get(mentor.getId());
            HostUniversity university = mentorIdToUniversity.get(mentor.getId());
            boolean isApplied = mentorIdToIsApplied.get(mentor.getId());
            String termName = termIdToName.get(mentor.getTermId());
            MentorPreviewResponse response = MentorPreviewResponse.of(mentor, mentorUser, university, isApplied, termName);
            mentorPreviews.add(response);
        }
        return mentorPreviews;
    }
}
