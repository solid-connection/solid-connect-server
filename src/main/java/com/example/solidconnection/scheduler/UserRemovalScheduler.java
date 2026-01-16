package com.example.solidconnection.scheduler;

import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.chat.repository.ChatParticipantRepository;
import com.example.solidconnection.chat.repository.ChatReadStatusRepository;
import com.example.solidconnection.community.comment.repository.CommentRepository;
import com.example.solidconnection.community.post.repository.PostLikeRepository;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.location.country.repository.InterestedCountryRepository;
import com.example.solidconnection.location.region.repository.InterestedRegionRepository;
import com.example.solidconnection.mentor.repository.MentorApplicationRepository;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.mentor.repository.MentoringRepository;
import com.example.solidconnection.news.repository.LikedNewsRepository;
import com.example.solidconnection.news.repository.NewsRepository;
import com.example.solidconnection.report.repository.ReportRepository;
import com.example.solidconnection.s3.service.S3Service;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.siteuser.repository.UserBlockRepository;
import com.example.solidconnection.university.repository.LikedUnivApplyInfoRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UserRemovalScheduler {

    public static final String EVERY_MIDNIGHT = "0 0 0 * * ?";
    public static final int ACCOUNT_RECOVER_DURATION = 30;

    private final SiteUserRepository siteUserRepository;
    private final InterestedCountryRepository interestedCountryRepository;
    private final InterestedRegionRepository interestedRegionRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final LikedUnivApplyInfoRepository likedUnivApplyInfoRepository;
    private final ApplicationRepository applicationRepository;
    private final GpaScoreRepository gpaScoreRepository;
    private final LanguageTestScoreRepository languageTestScoreRepository;
    private final MentorRepository mentorRepository;
    private final MentoringRepository mentoringRepository;
    private final NewsRepository newsRepository;
    private final LikedNewsRepository likedNewsRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatReadStatusRepository chatReadStatusRepository;
    private final ReportRepository reportRepository;
    private final UserBlockRepository userBlockRepository;
    private final MentorApplicationRepository mentorApplicationRepository;
    private final S3Service s3Service;

    /*
     * 탈퇴 후 계정 복구 기한까지 방문하지 않은 사용자를 삭제한다.
     * */
    @Scheduled(cron = EVERY_MIDNIGHT)
    @Transactional
    public void scheduledUserRemoval() {
        LocalDate cutoffDate = LocalDate.now().minusDays(ACCOUNT_RECOVER_DURATION);
        List<SiteUser> usersToRemove = siteUserRepository.findUsersToBeRemoved(cutoffDate);

        usersToRemove.forEach(this::deleteUserAndRelatedData);
    }

    private void deleteUserAndRelatedData(SiteUser user) {
        long siteUserId = user.getId();

        likedNewsRepository.deleteAllBySiteUserId(siteUserId);
        newsRepository.deleteAllBySiteUserId(siteUserId);

        postLikeRepository.deleteAllBySiteUserId(siteUserId);
        commentRepository.deleteAllBySiteUserId(siteUserId);
        postRepository.deleteAllBySiteUserId(siteUserId);

        mentoringRepository.deleteAllByMenteeId(siteUserId);
        mentorRepository.deleteAllBySiteUserId(siteUserId);
        mentorApplicationRepository.deleteAllBySiteUserId(siteUserId);

        List<Long> chatParticipantIds = chatParticipantRepository.findAllIdsBySiteUserId(siteUserId);
        chatReadStatusRepository.deleteAllByChatParticipantIdIn(chatParticipantIds);
        chatParticipantRepository.deleteAllBySiteUserId(siteUserId);
        reportRepository.deleteAllByReporterId(siteUserId);
        userBlockRepository.deleteAllByBlockerIdOrBlockedId(siteUserId, siteUserId);

        applicationRepository.deleteAllBySiteUserId(siteUserId);
        gpaScoreRepository.deleteAllBySiteUserId(siteUserId);
        languageTestScoreRepository.deleteAllBySiteUserId(siteUserId);
        likedUnivApplyInfoRepository.deleteAllBySiteUserId(siteUserId);
        interestedCountryRepository.deleteAllBySiteUserId(siteUserId);
        interestedRegionRepository.deleteAllBySiteUserId(siteUserId);

        s3Service.deleteExProfile(siteUserId);

        siteUserRepository.delete(user);
    }
}
