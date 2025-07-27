package com.example.solidconnection.chat.service;

import static com.example.solidconnection.common.exception.ErrorCode.CHAT_PARTNER_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_CHAT_ROOM_STATE;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.domain.ChatParticipant;
import com.example.solidconnection.chat.domain.ChatRoom;
import com.example.solidconnection.chat.dto.ChatParticipantResponse;
import com.example.solidconnection.chat.dto.ChatRoomListResponse;
import com.example.solidconnection.chat.dto.ChatRoomResponse;
import com.example.solidconnection.chat.repository.ChatRoomRepository;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ChatService {

    public static final int CHAT_PARTNER_LIMIT = 1;

    private final ChatRoomRepository chatRoomRepository;
    private final SiteUserRepository siteUserRepository;

    @Transactional(readOnly = true)
    public ChatRoomListResponse getChatRooms(long siteUserId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findOneOnOneChatRoomsByUserId(siteUserId);
        List<ChatRoomResponse> chatRoomInfos = chatRooms.stream()
                .map(chatRoom -> toChatRoomResponse(chatRoom, siteUserId))
                .toList();
        return ChatRoomListResponse.of(chatRoomInfos);
    }

    private ChatRoomResponse toChatRoomResponse(ChatRoom chatRoom, long siteUserId) {
        Optional<ChatMessage> latestMessage = chatRoomRepository.findLatestMessageByChatRoomId(chatRoom.getId());
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
        List<ChatParticipant> partners = chatRoom.getChatParticipants().stream()
                .filter(participant -> participant.getSiteUserId() != siteUserId)
                .toList();
        validateOneOnOneChat(partners);
        return partners.get(0);
    }

    private void validateOneOnOneChat(List<ChatParticipant> partners) {
        if (partners.isEmpty()) {
            throw new CustomException(CHAT_PARTNER_NOT_FOUND);
        }
        if (partners.size() > CHAT_PARTNER_LIMIT) {
            throw new CustomException(INVALID_CHAT_ROOM_STATE);
        }
    }
}
