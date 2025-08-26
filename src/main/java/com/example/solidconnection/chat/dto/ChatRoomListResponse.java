package com.example.solidconnection.chat.dto;

import java.util.List;

public record ChatRoomListResponse(
        List<ChatRoomResponse> chatRooms
) {

    public static ChatRoomListResponse of(List<ChatRoomResponse> chatRooms) {
        return new ChatRoomListResponse(chatRooms);
    }
}
