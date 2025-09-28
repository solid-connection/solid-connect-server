package com.example.solidconnection.chat.dto;

import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.domain.MessageType;
import java.util.List;

public record ChatMessageSendResponse(
        long messageId,
        String content,
        long senderId,
        MessageType messageType,
        List<ChatAttachmentResponse> attachments
) {

    public static ChatMessageSendResponse from(ChatMessage chatMessage) {
        MessageType messageType = chatMessage.getChatAttachments().isEmpty()
                ? MessageType.TEXT
                : MessageType.IMAGE;

        List<ChatAttachmentResponse> attachments = chatMessage.getChatAttachments().stream()
                .map(attachment -> ChatAttachmentResponse.of(
                        attachment.getId(),
                        attachment.getIsImage(),
                        attachment.getUrl(),
                        attachment.getThumbnailUrl(),
                        attachment.getCreatedAt()
                ))
                .toList();

        return new ChatMessageSendResponse(
                chatMessage.getId(),
                chatMessage.getContent(),
                chatMessage.getSenderId(),
                messageType,
                attachments
        );
    }
}
