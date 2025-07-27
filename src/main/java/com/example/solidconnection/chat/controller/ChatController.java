package com.example.solidconnection.chat.controller;

import com.example.solidconnection.chat.dto.ChatRoomListResponse;
import com.example.solidconnection.chat.service.ChatService;
import com.example.solidconnection.common.resolver.AuthorizedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chats")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/rooms")
    public ResponseEntity<ChatRoomListResponse> getChatRooms(
            @AuthorizedUser long siteUserId
    ) {
        ChatRoomListResponse chatRoomListResponse = chatService.getChatRooms(siteUserId);
        return ResponseEntity.ok(chatRoomListResponse);
    }
}
