package com.example.solidconnection.chat.service;

import static com.example.solidconnection.common.exception.ErrorCode.CHAT_PARTICIPANT_NOT_FOUND;

import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.domain.ChatParticipant;
import com.example.solidconnection.chat.dto.ChatAttachmentResponse;
import com.example.solidconnection.chat.dto.ChatMessageHistoryResult;
import com.example.solidconnection.chat.dto.ChatMessageResponse;
import com.example.solidconnection.chat.repository.ChatMessageRepository;
import com.example.solidconnection.chat.repository.ChatParticipantRepository;
import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.exception.CustomException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatMessageHistoryQueryService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Transactional(readOnly = true)
    public ChatMessageHistoryResult getChatMessages(
            long siteUserId,
            long roomId,
            Pageable pageable
    ) {
        ChatParticipant participant = chatParticipantRepository.findByChatRoomIdAndSiteUserId(roomId, siteUserId)
                .orElseThrow(() -> new CustomException(CHAT_PARTICIPANT_NOT_FOUND));
        Slice<ChatMessage> chatMessages = chatMessageRepository.findByRoomIdWithPaging(roomId, pageable);
        Map<Long, ChatParticipant> participants = findParticipants(chatMessages);
        List<ChatMessageResponse> content = toResponses(chatMessages, participants);

        return new ChatMessageHistoryResult(
                SliceResponse.of(content, chatMessages),
                participant.getId(),
                chatMessages.getContent().stream().map(ChatMessage::getId).toList()
        );
    }

    private Map<Long, ChatParticipant> findParticipants(Slice<ChatMessage> chatMessages) {
        Set<Long> participantIds = chatMessages.getContent().stream()
                .map(ChatMessage::getSenderId)
                .collect(Collectors.toSet());

        return chatParticipantRepository.findAllById(participantIds).stream()
                .collect(Collectors.toMap(ChatParticipant::getId, Function.identity()));
    }

    private List<ChatMessageResponse> toResponses(
            Slice<ChatMessage> chatMessages,
            Map<Long, ChatParticipant> participants
    ) {
        return chatMessages.getContent().stream()
                .map(message -> toResponse(message, participants))
                .toList();
    }

    private ChatMessageResponse toResponse(ChatMessage message, Map<Long, ChatParticipant> participants) {
        ChatParticipant sender = participants.get(message.getSenderId());
        if (sender == null) {
            throw new CustomException(CHAT_PARTICIPANT_NOT_FOUND);
        }
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
                sender.getSiteUserId(),
                message.getCreatedAt(),
                attachments
        );
    }
}
