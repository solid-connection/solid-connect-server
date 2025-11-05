package com.example.solidconnection.chat.service;

import static com.example.solidconnection.common.exception.ErrorCode.CHAT_PARTICIPANT_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.CHAT_PARTNER_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_CHAT_ROOM_STATE;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import com.example.solidconnection.chat.domain.ChatAttachment;
import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.domain.ChatParticipant;
import com.example.solidconnection.chat.domain.ChatRoom;
import com.example.solidconnection.chat.dto.ChatAttachmentResponse;
import com.example.solidconnection.chat.dto.ChatImageSendRequest;
import com.example.solidconnection.chat.dto.ChatMessageResponse;
import com.example.solidconnection.chat.dto.ChatMessageSendRequest;
import com.example.solidconnection.chat.dto.ChatMessageSendResponse;
import com.example.solidconnection.chat.dto.ChatParticipantResponse;
import com.example.solidconnection.chat.dto.ChatRoomData;
import com.example.solidconnection.chat.dto.ChatRoomListResponse;
import com.example.solidconnection.chat.dto.ChatRoomResponse;
import com.example.solidconnection.chat.repository.ChatMessageRepository;
import com.example.solidconnection.chat.repository.ChatParticipantRepository;
import com.example.solidconnection.chat.repository.ChatReadStatusRepository;
import com.example.solidconnection.chat.repository.ChatRoomRepository;
import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.mentor.domain.Mentor;
import com.example.solidconnection.mentor.repository.MentorRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatReadStatusRepository chatReadStatusRepository;
    private final SiteUserRepository siteUserRepository;
    private final MentorRepository mentorRepository;

    private final SimpMessageSendingOperations simpMessageSendingOperations;

    public ChatService(ChatRoomRepository chatRoomRepository,
                       ChatMessageRepository chatMessageRepository,
                       ChatParticipantRepository chatParticipantRepository,
                       ChatReadStatusRepository chatReadStatusRepository,
                       SiteUserRepository siteUserRepository,
                       MentorRepository mentorRepository,
                       @Lazy SimpMessageSendingOperations simpMessageSendingOperations) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatReadStatusRepository = chatReadStatusRepository;
        this.siteUserRepository = siteUserRepository;
        this.mentorRepository = mentorRepository;
        this.simpMessageSendingOperations = simpMessageSendingOperations;
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

    @Transactional(readOnly = true)
    public SliceResponse<ChatMessageResponse> getChatMessages(long siteUserId, long roomId, Pageable pageable) {
        validateChatRoomParticipant(siteUserId, roomId);

        Slice<ChatMessage> chatMessages = chatMessageRepository.findByRoomIdWithPaging(roomId, pageable);

        // senderId(participantId) 조회
        Set<Long> participantIds = chatMessages.getContent().stream()
                .map(ChatMessage::getSenderId)
                .collect(Collectors.toSet());

        Map<Long, ChatParticipant> participantIdToParticipant = chatParticipantRepository.findAllById(participantIds).stream()
                .collect(Collectors.toMap(ChatParticipant::getId, Function.identity()));

        // participants의 siteUserId의 집합
        Set<Long> siteUserIds = participantIdToParticipant.values().stream()
                .map(ChatParticipant::getSiteUserId)
                .collect(Collectors.toSet());

        Map<Long, Long> siteUserIdToMentorId = mentorRepository.findAllBySiteUserIdIn(siteUserIds.stream().toList()).stream()
                .collect(Collectors.toMap(Mentor::getSiteUserId, Mentor::getId));

        List<ChatMessageResponse> content = chatMessages.getContent().stream()
                .map(message -> {
                    ChatParticipant senderParticipant = participantIdToParticipant.get(message.getSenderId());
                    if (senderParticipant == null) {
                        throw new CustomException(CHAT_PARTICIPANT_NOT_FOUND);
                    }
                    long externalSenderId = siteUserIdToMentorId.getOrDefault(
                            senderParticipant.getSiteUserId(),
                            senderParticipant.getSiteUserId()
                    );
                    return toChatMessageResponse(message, externalSenderId);
                })
                .toList();

        return SliceResponse.of(content, chatMessages);
    }

    @Transactional(readOnly = true)
    public ChatParticipantResponse getChatPartner(long siteUserId, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(INVALID_CHAT_ROOM_STATE));
        ChatParticipant partnerParticipant = findPartner(chatRoom, siteUserId);
        SiteUser siteUser = siteUserRepository.findById(partnerParticipant.getSiteUserId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 멘티는 siteUserId, 멘토는 mentorId
        Long partnerId = mentorRepository.findBySiteUserId(siteUser.getId())
                .map(Mentor::getId)
                .orElse(siteUser.getId());

        return ChatParticipantResponse.of(partnerId, siteUser.getNickname(), siteUser.getProfileImageUrl());
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
        boolean isParticipant = chatParticipantRepository.existsByChatRoomIdAndSiteUserId(roomId, siteUserId);
        if (!isParticipant) {
            throw new CustomException(CHAT_PARTICIPANT_NOT_FOUND);
        }
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessage message, long externalSenderId) {
        List<ChatAttachmentResponse> attachments = message.getChatAttachments().stream()
                .map(attachment -> ChatAttachmentResponse.of(
                        attachment.getId(),
                        attachment.getIsImage(),
                        attachment.getUrl(),
                        attachment.getThumbnailUrl(),
                        attachment.getCreatedAt()
                ))
                .toList();

        return ChatMessageResponse.of(
                message.getId(),
                message.getContent(),
                externalSenderId,
                message.getCreatedAt(),
                attachments
        );
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
        long senderId = chatParticipantRepository.findByChatRoomIdAndSiteUserId(roomId, siteUserId)
                .orElseThrow(() -> new CustomException(CHAT_PARTICIPANT_NOT_FOUND))
                .getId();

        ChatMessage chatMessage = new ChatMessage(
                chatMessageSendRequest.content(),
                senderId,
                chatRoomRepository.findById(roomId)
                        .orElseThrow(() -> new CustomException(INVALID_CHAT_ROOM_STATE))
        );

        chatMessageRepository.save(chatMessage);

        ChatMessageSendResponse chatMessageResponse = ChatMessageSendResponse.from(chatMessage);

        simpMessageSendingOperations.convertAndSend("/topic/chat/" + roomId, chatMessageResponse);
    }

    @Transactional
    public void sendChatImage(ChatImageSendRequest chatImageSendRequest, long siteUserId, long roomId) {
        long senderId = chatParticipantRepository.findByChatRoomIdAndSiteUserId(roomId, siteUserId)
                .orElseThrow(() -> new CustomException(CHAT_PARTICIPANT_NOT_FOUND))
                .getId();

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(INVALID_CHAT_ROOM_STATE));

        ChatMessage chatMessage = new ChatMessage(
                "",
                senderId,
                chatRoom
        );

        for (String imageUrl : chatImageSendRequest.imageUrls()) {
            String thumbnailUrl = generateThumbnailUrl(imageUrl);

            ChatAttachment attachment = new ChatAttachment(true, imageUrl, thumbnailUrl, null);
            chatMessage.addAttachment(attachment);
        }

        chatMessageRepository.save(chatMessage);

        ChatMessageSendResponse chatMessageResponse = ChatMessageSendResponse.from(chatMessage);
        simpMessageSendingOperations.convertAndSend("/topic/chat/" + roomId, chatMessageResponse);
    }

    private String generateThumbnailUrl(String originalUrl) {
        try {
            String fileName = originalUrl.substring(originalUrl.lastIndexOf('/') + 1);

            String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));

            String thumbnailFileName = nameWithoutExt + "_thumb" + extension;

            String thumbnailUrl = originalUrl.replace("chat/images/", "chat/thumbnails/")
                    .replace(fileName, thumbnailFileName);

            return thumbnailUrl;

        } catch (Exception e) {
            return originalUrl;
        }
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
