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
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
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
        // todo : n + 1 문제 해결 필요!
        List<ChatRoom> chatRooms = chatRoomRepository.findOneOnOneChatRoomsByUserId(siteUserId);
        List<ChatRoomResponse> chatRoomInfos = chatRooms.stream()
                .map(chatRoom -> toChatRoomResponse(chatRoom, siteUserId))
                .toList();
        return ChatRoomListResponse.of(chatRoomInfos);
    }

    private ChatRoomResponse toChatRoomResponse(ChatRoom chatRoom, long siteUserId) {
        Optional<ChatMessage> latestMessage = chatMessageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId());
        String lastChatMessage = latestMessage.map(ChatMessage::getContent).orElse("");
        ZonedDateTime lastReceivedTime = latestMessage.map(ChatMessage::getCreatedAt).orElse(null);

        ChatParticipant partnerParticipant = findPartner(chatRoom, siteUserId);

        SiteUser siteUser = siteUserRepository.findById(partnerParticipant.getSiteUserId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        ChatParticipantResponse partner = ChatParticipantResponse.of(siteUser.getId(), siteUser.getNickname(), siteUser.getProfileImageUrl());

        long unReadCount = chatRoomRepository.countUnreadMessages(chatRoom.getId(), siteUserId);

        return ChatRoomResponse.of(chatRoom.getId(), lastChatMessage, lastReceivedTime, partner, unReadCount);
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
