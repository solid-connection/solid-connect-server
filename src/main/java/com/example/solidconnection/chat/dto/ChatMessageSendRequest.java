package com.example.solidconnection.chat.dto;

public record ChatMessageSendRequest(
        long senderId,
        String content
) {

}
