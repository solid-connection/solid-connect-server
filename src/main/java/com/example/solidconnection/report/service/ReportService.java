package com.example.solidconnection.report.service;

import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.domain.ChatParticipant;
import com.example.solidconnection.chat.repository.ChatMessageRepository;
import com.example.solidconnection.chat.repository.ChatParticipantRepository;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.report.domain.Report;
import com.example.solidconnection.report.domain.TargetType;
import com.example.solidconnection.report.dto.ReportRequest;
import com.example.solidconnection.report.repository.ReportRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.domain.UserStatus;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final SiteUserRepository siteUserRepository;
    private final PostRepository postRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Transactional
    public void createReport(long reporterId, ReportRequest request) {
        long reportedId = findReportedId(request.targetType(), request.targetId());
        validateReporterAndReportedExists(reporterId, reportedId);
        validateTargetExists(request.targetType(), request.targetId());
        validateFirstReportByUser(reporterId, request.targetType(), request.targetId());
        updateUserStatusToReported(reportedId);

        Report report = new Report(reporterId, reportedId, request.reportType(), request.targetType(), request.targetId());
        reportRepository.save(report);
    }

    private void validateReporterAndReportedExists(long reporterId, long reportedId) {
        if (!siteUserRepository.existsById(reporterId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        if (!siteUserRepository.existsById(reportedId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private void validateTargetExists(TargetType targetType, long targetId) {
        boolean exists = switch (targetType) {
            case POST -> postRepository.existsById(targetId);
            case CHAT -> chatMessageRepository.existsById(targetId);
        };

        if (!exists) {
            throw new CustomException(ErrorCode.REPORT_TARGET_NOT_FOUND);
        }
    }

    private void validateFirstReportByUser(long reporterId, TargetType targetType, long targetId) {
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, targetType, targetId)) {
            throw new CustomException(ErrorCode.ALREADY_REPORTED_BY_CURRENT_USER);
        }
    }

    private long findReportedId(TargetType targetType, long targetId) {
        return switch (targetType) {
            case POST -> findPostAuthorId(targetId);
            case CHAT -> findChatMessageSenderId(targetId);
        };
    }

    private long findPostAuthorId(long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_TARGET_NOT_FOUND));
        return post.getSiteUserId();
    }

    private long findChatMessageSenderId(long chatMessageId) {
        ChatMessage chatMessage = chatMessageRepository.findById(chatMessageId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_TARGET_NOT_FOUND));
        ChatParticipant chatParticipant = chatParticipantRepository.findById(chatMessage.getSenderId())
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));
        return chatParticipant.getSiteUserId();
    }

    private void updateUserStatusToReported(long userId) {
        SiteUser user = siteUserRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateUserStatus(UserStatus.REPORTED);
    }
}
