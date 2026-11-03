package com.example.solidconnection.chat.service;

import static com.example.solidconnection.common.exception.ErrorCode.CHAT_PARTICIPANT_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.CHAT_PARTNER_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_CHAT_ROOM_STATE;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MICROS;

import com.example.solidconnection.chat.domain.ChatAttachment;
import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.domain.ChatMessageOutbox;
import com.example.solidconnection.chat.domain.ChatParticipant;
import com.example.solidconnection.chat.domain.ChatRoom;
import com.example.solidconnection.chat.dto.ChatImageSendRequest;
import com.example.solidconnection.chat.dto.ChatMessageHistoryResult;
import com.example.solidconnection.chat.dto.ChatMessageResponse;
import com.example.solidconnection.chat.dto.ChatMessageSendRequest;
import com.example.solidconnection.chat.dto.ChatParticipantResponse;
import com.example.solidconnection.chat.dto.ChatRoomData;
import com.example.solidconnection.chat.dto.ChatRoomListResponse;
import com.example.solidconnection.chat.dto.ChatRoomResponse;
import com.example.solidconnection.chat.event.ChatMessageBroadcastEvent;
import com.example.solidconnection.chat.repository.ChatMessageRepository;
import com.example.solidconnection.chat.repository.ChatMessageOutboxRepository;
import com.example.solidconnection.chat.repository.ChatParticipantRepository;
import com.example.solidconnection.chat.repository.ChatReadStatusRepository;
import com.example.solidconnection.chat.repository.ChatRoomRepository;
import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageOutboxRepository chatMessageOutboxRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatReadStatusRepository chatReadStatusRepository;
    private final SiteUserRepository siteUserRepository;
    private final MentorRepository mentorRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final ChatMessageHistoryQueryService chatMessageHistoryQueryService;
    private final ChatMessageOutboxTransactionService outboxTransactionService;

    public ChatService(ChatRoomRepository chatRoomRepository,
                       ChatMessageRepository chatMessageRepository,
                       ChatMessageOutboxRepository chatMessageOutboxRepository,
                       ChatParticipantRepository chatParticipantRepository,
                       ChatReadStatusRepository chatReadStatusRepository,
                       SiteUserRepository siteUserRepository,
                       MentorRepository mentorRepository,
                       ApplicationEventPublisher eventPublisher,
                       ChatMessageHistoryQueryService chatMessageHistoryQueryService,
                       ChatMessageOutboxTransactionService outboxTransactionService) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatMessageOutboxRepository = chatMessageOutboxRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatReadStatusRepository = chatReadStatusRepository;
        this.siteUserRepository = siteUserRepository;
        this.mentorRepository = mentorRepository;
        this.eventPublisher = eventPublisher;
        this.chatMessageHistoryQueryService = chatMessageHistoryQueryService;
        this.outboxTransactionService = outboxTransactionService;
    }

    @Transactional(readOnly = true)
    public ChatRoomListResponse getChatRooms(long siteUserId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findOneOnOneChatRoomsByUserIdWithParticipants(siteUserId);

        if (chatRooms.isEmpty()) {
            return ChatRoomListResponse.of(Collections.emptyList());
        }

        ChatRoomData chatRoomData = getChatRoomData(chatRooms, siteUserId);

        List<ChatRoomResponse> responses = chatRooms.stream()
                .map(chatRoom -> createChatRoomResponse(chatRoom, siteUserId, chatRoomData))
                .toList();

        return ChatRoomListResponse.of(responses);
    }

    private ChatRoomData getChatRoomData(List<ChatRoom> chatRooms, long siteUserId) {
        List<Long> chatRoomIds = chatRooms.stream().map(ChatRoom::getId).toList();
        List<Long> partnerUserIds = chatRooms.stream()
                .map(chatRoom -> findPartner(chatRoom, siteUserId).getSiteUserId())
                .toList();

        return ChatRoomData.from(
                chatMessageRepository.findLatestMessagesByChatRoomIds(chatRoomIds),
                chatMessageRepository.countUnreadMessagesBatch(chatRoomIds, siteUserId),
                siteUserRepository.findAllByIdIn(partnerUserIds)
        );
    }

    private ChatRoomResponse createChatRoomResponse(ChatRoom chatRoom, long siteUserId, ChatRoomData chatRoomData) {
        ChatMessage latestMessage = chatRoomData.latestMessages().get(chatRoom.getId());
        ChatParticipant partner = findPartner(chatRoom, siteUserId);
        SiteUser partnerUser = chatRoomData.partnerUsers().get(partner.getSiteUserId());

        if (partnerUser == null) {
            throw new CustomException(USER_NOT_FOUND);
        }

        return ChatRoomResponse.of(
                chatRoom.getId(),
                latestMessage != null ? latestMessage.getContent() : "",
                latestMessage != null ? latestMessage.getCreatedAt() : null,
                ChatParticipantResponse.of(partnerUser.getId(), partnerUser.getNickname(), partnerUser.getProfileImageUrl()),
                chatRoomData.unreadCounts().getOrDefault(chatRoom.getId(), 0L)
        );
    }

    public SliceResponse<ChatMessageResponse> getChatMessages(long siteUserId, long roomId, Pageable pageable) {
        ChatMessageHistoryResult result =
                chatMessageHistoryQueryService.getChatMessages(siteUserId, roomId, pageable);
        try {
            outboxTransactionService.markHistoryServed(
                    result.participantId(),
                    result.messageIds(),
                    now()
            );
        } catch (RuntimeException e) {
            log.error("채팅 내역 조회 Outbox 완료 기록에 실패했습니다. participantId={}, roomId={}",
                    result.participantId(), roomId, e);
        }
        return result.response();
    }

    @Transactional(readOnly = true)
    public ChatParticipantResponse getChatPartner(long siteUserId, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(INVALID_CHAT_ROOM_STATE));
        ChatParticipant partnerParticipant = findPartner(chatRoom, siteUserId);
        SiteUser siteUser = siteUserRepository.findById(partnerParticipant.getSiteUserId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        return ChatParticipantResponse.of(siteUser.getId(), siteUser.getNickname(), siteUser.getProfileImageUrl());
    }

    private ChatParticipant findPartner(ChatRoom chatRoom, long siteUserId) {
        if (chatRoom.isGroup()) {
            throw new CustomException(INVALID_CHAT_ROOM_STATE);
        }
        return chatRoom.getChatParticipants().stream()
                .filter(participant -> participant.getSiteUserId() != siteUserId)
                .findFirst()
                .orElseThrow(() -> new CustomException(CHAT_PARTNER_NOT_FOUND));
    }

    public void validateChatRoomParticipant(long siteUserId, long roomId) {
        getChatParticipant(siteUserId, roomId);
    }

    @Transactional
    public void markChatMessagesAsRead(long siteUserId, long roomId) {
        ChatParticipant participant = chatParticipantRepository
                .findByChatRoomIdAndSiteUserId(roomId, siteUserId)
                .orElseThrow(() -> new CustomException(CHAT_PARTICIPANT_NOT_FOUND));

        chatReadStatusRepository.upsertReadStatus(roomId, participant.getId());
    }

    @Transactional
    public void sendChatMessage(ChatMessageSendRequest chatMessageSendRequest, long siteUserId, long roomId) {
        ChatParticipant sender = getChatParticipant(siteUserId, roomId);
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(INVALID_CHAT_ROOM_STATE));

        ChatMessage chatMessage = new ChatMessage(
                chatMessageSendRequest.content(),
                sender.getId(),
                chatRoom
        );

        chatMessageRepository.save(chatMessage);
        publishOutboxEvent(chatMessage, findOutboxRecipient(roomId, siteUserId));
    }

    @Transactional
    public void sendChatImage(ChatImageSendRequest chatImageSendRequest, long siteUserId, long roomId) {
        ChatParticipant sender = getChatParticipant(siteUserId, roomId);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(INVALID_CHAT_ROOM_STATE));

        ChatMessage chatMessage = new ChatMessage("", sender.getId(), chatRoom);

        // 이미지 판별을 위한 확장자 리스트
        List<String> imageExtensions = Arrays.asList("jpg", "jpeg", "png", "webp");

        for (String imageUrl : chatImageSendRequest.imageUrls()) {
            String extension = StringUtils.getFilenameExtension(imageUrl);

            boolean isImage = extension != null && imageExtensions.contains(extension.toLowerCase());

            String thumbnailUrl = isImage ? generateThumbnailUrl(imageUrl) : null;

            ChatAttachment attachment = new ChatAttachment(isImage, imageUrl, thumbnailUrl, null);
            chatMessage.addAttachment(attachment);
        }

        chatMessageRepository.save(chatMessage);
        publishOutboxEvent(chatMessage, findOutboxRecipient(roomId, siteUserId));
    }

    private ChatParticipant getChatParticipant(long siteUserId, long roomId) {
        return chatParticipantRepository.findByChatRoomIdAndSiteUserId(roomId, siteUserId)
                .orElseThrow(() -> new CustomException(CHAT_PARTICIPANT_NOT_FOUND));
    }

    private String generateThumbnailUrl(String originalUrl) {
        try {
            String fileName = originalUrl.substring(originalUrl.lastIndexOf('/') + 1);

            String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));

            String thumbnailFileName = nameWithoutExt + "_thumb" + extension;

            return originalUrl.replace("chat/files/", "chat/thumbnails/")
                    .replace(fileName, thumbnailFileName);

        } catch (Exception e) {
            return originalUrl;
        }
    }

    private ChatParticipant findOutboxRecipient(long roomId, long senderSiteUserId) {
        List<ChatParticipant> recipients =
                chatParticipantRepository.findAllByChatRoomIdAndSiteUserIdNot(roomId, senderSiteUserId);
        if (recipients.size() != 1) {
            throw new CustomException(INVALID_CHAT_ROOM_STATE);
        }
        return recipients.get(0);
    }

    private void publishOutboxEvent(ChatMessage chatMessage, ChatParticipant recipient) {
        ChatMessageOutbox outbox = chatMessageOutboxRepository.save(
                new ChatMessageOutbox(chatMessage, recipient, now())
        );
        eventPublisher.publishEvent(new ChatMessageBroadcastEvent(outbox.getId()));
    }

    private ZonedDateTime now() {
        return ZonedDateTime.now(UTC).truncatedTo(MICROS);
    }

    @Transactional
    public Long createMentoringChatRoom(Long mentoringId, Long mentorId, Long menteeId) {
        ChatRoom existingChatRoom = chatRoomRepository.findByMentoringId(mentoringId);
        if (existingChatRoom != null) {
            return existingChatRoom.getId();
        }

        // 새 채팅방 생성
        ChatRoom chatRoom = new ChatRoom(mentoringId, false);
        chatRoom = chatRoomRepository.save(chatRoom);

        ChatParticipant mentorParticipant = new ChatParticipant(mentorId, chatRoom);
        ChatParticipant menteeParticipant = new ChatParticipant(menteeId, chatRoom);
        chatParticipantRepository.saveAll(List.of(mentorParticipant, menteeParticipant));

        return chatRoom.getId();
    }
}
