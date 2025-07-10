package com.example.solidconnection.mentor.repository;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.domain.Mentoring;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.solidconnection.common.exception.ErrorCode.DATA_INTEGRITY_VIOLATION;

@Repository
@RequiredArgsConstructor
public class MentorBatchQueryRepository { // 연관관계가 설정되지 않은 엔티티들을 N+1 없이 하나의 쿼리로 조회

    private final SiteUserRepository siteUserRepository;
    private final MentoringRepository mentoringRepository;

    public Map<Long, SiteUser> getMentorIdToSiteUserMap(List<Mentor> mentors) {
        List<Long> mentorUserIds = mentors.stream().map(Mentor::getSiteUserId).toList();
        List<SiteUser> mentorUsers = siteUserRepository.findAllById(mentorUserIds);
        Map<Long, SiteUser> mentorUserIdToSiteUserMap = mentorUsers.stream()
                .collect(Collectors.toMap(SiteUser::getId, Function.identity()));

        return mentors.stream().collect(Collectors.toMap(
                Mentor::getId,
                mentor -> {
                    SiteUser mentorUser = mentorUserIdToSiteUserMap.get(mentor.getSiteUserId());
                    if (mentorUser == null) { // site_user.id == mentor.site_user_id 에 해당하는게 없으면 정합성 문제가 발생한 것
                        throw new CustomException(DATA_INTEGRITY_VIOLATION, "mentor에 해당하는 siteUser 존재하지 않음");
                    }
                    return mentorUser;
                }
        ));
    }

    public Map<Long, Boolean> getMentorIdToIsApplied(List<Mentor> mentors, long currentUserId) {
        List<Long> mentorIds = mentors.stream().map(Mentor::getId).toList();
        List<Mentoring> appliedMentorings = mentoringRepository.findAllByMentorIdInAndMenteeId(mentorIds, currentUserId);
        Set<Long> appliedMentorIds = appliedMentorings.stream()
                .map(Mentoring::getMentorId)
                .collect(Collectors.toSet());

        return mentors.stream().collect(Collectors.toMap(
                Mentor::getId,
                mentor -> appliedMentorIds.contains(mentor.getId())
        ));
    }
}
