package com.example.solidconnection.scheduler;

import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.chat.repository.ChatMessageRepository;
import com.example.solidconnection.chat.repository.ChatParticipantRepository;
import com.example.solidconnection.chat.repository.ChatReadStatusRepository;
import com.example.solidconnection.chat.repository.ChatRoomRepository;
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
import com.example.solidconnection.siteuser.repository.UserBanRepository;
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
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatReadStatusRepository chatReadStatusRepository;
    private final ReportRepository reportRepository;
    private final UserBlockRepository userBlockRepository;
    private final UserBanRepository userBanRepository;
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
        deleteNews(siteUserId);

        postLikeRepository.deleteAllBySiteUserId(siteUserId);
        commentRepository.deleteAllBySiteUserId(siteUserId);
        deletePosts(siteUserId);

        deleteChatRooms(siteUserId);
        deleteMentorings(siteUserId);
        mentorApplicationRepository.deleteAllBySiteUserId(siteUserId);

        reportRepository.deleteAllByReporterId(siteUserId);
        userBlockRepository.deleteAllByBlockerIdOrBlockedId(siteUserId, siteUserId);
        deleteUserBans(siteUserId);

        applicationRepository.deleteAllBySiteUserId(siteUserId);
        gpaScoreRepository.deleteAllBySiteUserId(siteUserId);
        languageTestScoreRepository.deleteAllBySiteUserId(siteUserId);
        likedUnivApplyInfoRepository.deleteAllBySiteUserId(siteUserId);
        interestedCountryRepository.deleteAllBySiteUserId(siteUserId);
        interestedRegionRepository.deleteAllBySiteUserId(siteUserId);

        s3Service.deleteExProfile(siteUserId);

        siteUserRepository.delete(user);
    }

    /*
     * 다른 사용자가 누른 좋아요가 남아 있으면 뉴스를 삭제할 수 없으므로 함께 삭제한다.
     * */
    private void deleteNews(long siteUserId) {
        List<Long> newsIds = newsRepository.findAllIdsBySiteUserId(siteUserId);
        if (!newsIds.isEmpty()) {
            likedNewsRepository.deleteAllByNewsIdIn(newsIds);
        }
        newsRepository.deleteAllBySiteUserId(siteUserId);
    }

    /*
     * 신고로 가려진 게시글은 조회 필터에 걸려 삭제되지 않으므로, 플래그를 되돌린 뒤 삭제한다.
     * */
    private void deletePosts(long siteUserId) {
        postRepository.unmarkDeletedBySiteUserId(siteUserId);
        postRepository.deleteAllBySiteUserId(siteUserId);
    }

    /*
     * 1:1 채팅방은 참여자 한 명이 사라지면 유지될 수 없으므로 방 전체를 삭제한다.
     * - 신고로 가려진 메시지는 조회 필터에 걸리므로, 플래그를 되돌린 뒤 삭제한다.
     * - 채팅방이 멘토링을 참조하므로 멘토링보다 먼저 삭제한다.
     * */
    private void deleteChatRooms(long siteUserId) {
        List<Long> chatRoomIds = chatParticipantRepository.findAllChatRoomIdsBySiteUserId(siteUserId);
        if (chatRoomIds.isEmpty()) {
            return;
        }
        chatMessageRepository.unmarkDeletedByChatRoomIdIn(chatRoomIds);
        chatMessageRepository.deleteAllByChatRoomIdIn(chatRoomIds);
        chatReadStatusRepository.deleteAllByChatRoomIdIn(chatRoomIds);
        chatParticipantRepository.deleteAllByChatRoomIdIn(chatRoomIds);
        chatRoomRepository.deleteAllById(chatRoomIds);
    }

    /*
     * 멘티로 참여한 멘토링과 멘토로 참여한 멘토링을 모두 삭제한 뒤 멘토를 삭제한다.
     * */
    private void deleteMentorings(long siteUserId) {
        mentoringRepository.deleteAllByMenteeId(siteUserId);
        List<Long> mentorIds = mentorRepository.findAllIdsBySiteUserId(siteUserId);
        if (!mentorIds.isEmpty()) {
            mentoringRepository.deleteAllByMentorIdIn(mentorIds);
        }
        mentorRepository.deleteAllBySiteUserId(siteUserId);
    }

    /*
     * 탈퇴자를 대상으로 한 정지 기록은 삭제한다.
     * 탈퇴자가 집행한 정지 기록은 다른 사용자의 이력이므로, 집행자 참조만 해제하고 보존한다.
     * */
    private void deleteUserBans(long siteUserId) {
        userBanRepository.deleteAllByBannedUserId(siteUserId);
        userBanRepository.clearBannedBy(siteUserId);
        userBanRepository.clearUnbannedBy(siteUserId);
    }
}
