package com.example.solidconnection.chat.controller;

import com.example.solidconnection.chat.dto.ChatMessageResponse;
import com.example.solidconnection.chat.dto.ChatParticipantResponse;
import com.example.solidconnection.chat.dto.ChatRoomListResponse;
import com.example.solidconnection.chat.service.ChatService;
import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.resolver.AuthorizedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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

    @GetMapping("/rooms/{room-id}")
    public ResponseEntity<SliceResponse<ChatMessageResponse>> getChatMessages(
            @AuthorizedUser long siteUserId,
            @PathVariable("room-id") Long roomId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        SliceResponse<ChatMessageResponse> response = chatService.getChatMessages(siteUserId, roomId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("rooms/{room-id}/partner")
    public ResponseEntity<ChatParticipantResponse> getChatPartner(
            @AuthorizedUser long siteUserId,
            @PathVariable("room-id") Long roomId
    ) {
        ChatParticipantResponse response = chatService.getChatPartner(siteUserId, roomId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/rooms/{room-id}/read")
    public ResponseEntity<Void> markChatMessagesAsRead(
            @AuthorizedUser long siteUserId,
            @PathVariable("room-id") Long roomId
    ) {
        chatService.markChatMessagesAsRead(siteUserId, roomId);
        return ResponseEntity.ok().build();
    }
}
