package com.example.solidconnection.chat.controller;

import com.example.solidconnection.chat.dto.ChatMessageSendRequest;
import com.example.solidconnection.chat.service.ChatService;
import com.example.solidconnection.common.resolver.AuthorizedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatService chatService;

    @MessageMapping("/chat/{roomId}")
    public void sendChatMessage(
            @DestinationVariable Long roomId,
            @Payload ChatMessageSendRequest chatMessageSendRequest,
            @AuthorizedUser Long siteUserId
    ) {
        chatService.sendChatMessage(chatMessageSendRequest, siteUserId, roomId);
    }
}
