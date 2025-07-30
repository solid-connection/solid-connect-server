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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatReadStatusRepository chatReadStatusRepository;
    private final SiteUserRepository siteUserRepository;

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

    private void validateChatRoomParticipant(long siteUserId, long roomId) {
        boolean isParticipant = chatParticipantRepository.existsByChatRoomIdAndSiteUserId(roomId, siteUserId);
        if (!isParticipant) {
            throw new CustomException(CHAT_PARTICIPANT_NOT_FOUND);
        }
    }
}
