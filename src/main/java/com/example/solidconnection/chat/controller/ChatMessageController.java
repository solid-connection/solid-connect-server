package com.example.solidconnection.chat.controller;

import com.example.solidconnection.chat.dto.ChatImageSendRequest;
import com.example.solidconnection.chat.dto.ChatMessageSendRequest;
import com.example.solidconnection.chat.service.ChatService;
import com.example.solidconnection.security.authentication.TokenAuthentication;
import com.example.solidconnection.security.userdetails.SiteUserDetails;
import jakarta.validation.Valid;
import java.security.Principal;
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
            @Valid @Payload ChatMessageSendRequest chatMessageSendRequest,
            Principal principal
    ) {
        TokenAuthentication tokenAuthentication = (TokenAuthentication) principal;
        SiteUserDetails siteUserDetails = (SiteUserDetails) tokenAuthentication.getPrincipal();

        chatService.sendChatMessage(chatMessageSendRequest, siteUserDetails.getSiteUser().getId(), roomId);
    }

    @MessageMapping("/chat/{roomId}/image")
    public void sendChatImage(
            @DestinationVariable Long roomId,
            @Valid @Payload ChatImageSendRequest chatImageSendRequest,
            Principal principal
    ) {
        TokenAuthentication tokenAuthentication = (TokenAuthentication) principal;
        SiteUserDetails siteUserDetails = (SiteUserDetails) tokenAuthentication.getPrincipal();

        chatService.sendChatImage(chatImageSendRequest, siteUserDetails.getSiteUser().getId(), roomId);
    }
}
