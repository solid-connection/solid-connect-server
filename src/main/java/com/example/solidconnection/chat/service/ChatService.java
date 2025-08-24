package com.example.solidconnection.chat.service;

import static com.example.solidconnection.common.exception.ErrorCode.CHAT_PARTICIPANT_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.CHAT_PARTNER_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_CHAT_ROOM_STATE;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.domain.ChatParticipant;
import com.example.solidconnection.chat.domain.ChatRoom;
import com.example.solidconnection.chat.dto.ChatAttachmentResponse;
import com.example.solidconnection.chat.dto.ChatMessageResponse;
import com.example.solidconnection.chat.dto.ChatMessageSendRequest;
import com.example.solidconnection.chat.dto.ChatMessageSendResponse;
import com.example.solidconnection.chat.dto.ChatParticipantResponse;
import com.example.solidconnection.chat.dto.ChatRoomListResponse;
import com.example.solidconnection.chat.dto.ChatRoomResponse;
import com.example.solidconnection.chat.repository.ChatMessageRepository;
import com.example.solidconnection.chat.repository.ChatParticipantRepository;
import com.example.solidconnection.chat.repository.ChatReadStatusRepository;
import com.example.solidconnection.chat.repository.ChatRoomRepository;
import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.chat.dto.ChatRoomData;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import java.util.Collections;
import java.util.List;
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

    private final SimpMessageSendingOperations simpMessageSendingOperations;

    public ChatService(ChatRoomRepository chatRoomRepository,
                       ChatMessageRepository chatMessageRepository,
                       ChatParticipantRepository chatParticipantRepository,
                       ChatReadStatusRepository chatReadStatusRepository,
                       SiteUserRepository siteUserRepository,
                       @Lazy SimpMessageSendingOperations simpMessageSendingOperations) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatReadStatusRepository = chatReadStatusRepository;
        this.siteUserRepository = siteUserRepository;
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

    private ChatParticipant findPartner(ChatRoom chatRoom, long siteUserId) {
        if (chatRoom.isGroup()) {
            throw new CustomException(INVALID_CHAT_ROOM_STATE);
        }
        return chatRoom.getChatParticipants().stream()
                .filter(participant -> participant.getSiteUserId() != siteUserId)
                .findFirst()
                .orElseThrow(() -> new CustomException(CHAT_PARTNER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public SliceResponse<ChatMessageResponse> getChatMessages(long siteUserId, long roomId, Pageable pageable) {
        validateChatRoomParticipant(siteUserId, roomId);

        Slice<ChatMessage> chatMessages = chatMessageRepository.findByRoomIdWithPaging(roomId, pageable);

        List<ChatMessageResponse> content = chatMessages.getContent().stream()
                .map(this::toChatMessageResponse)
                .toList();

        return SliceResponse.of(content, chatMessages);
    }

    public void validateChatRoomParticipant(long siteUserId, long roomId) {
        boolean isParticipant = chatParticipantRepository.existsByChatRoomIdAndSiteUserId(roomId, siteUserId);
        if (!isParticipant) {
            throw new CustomException(CHAT_PARTICIPANT_NOT_FOUND);
        }
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessage message) {
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
                message.getSenderId(),
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
    public void createMentoringChatRoom(Long mentoringId, Long mentorId, Long menteeId) {
        if (chatRoomRepository.existsByMentoringId(mentoringId)) {
            return;
        }

        ChatRoom chatRoom = new ChatRoom(mentoringId, false);
        chatRoom = chatRoomRepository.save(chatRoom);
        ChatParticipant mentorParticipant = new ChatParticipant(mentorId, chatRoom);
        ChatParticipant menteeParticipant = new ChatParticipant(menteeId, chatRoom);
        chatParticipantRepository.saveAll(List.of(mentorParticipant, menteeParticipant));
    }
}
