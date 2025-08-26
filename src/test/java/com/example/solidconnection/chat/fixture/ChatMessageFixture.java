package com.example.solidconnection.chat.fixture;

import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.domain.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ChatMessageFixture {

    private final ChatMessageFixtureBuilder chatMessageFixtureBuilder;

    public ChatMessage 메시지(String content, long senderId, ChatRoom chatRoom) {
        return chatMessageFixtureBuilder.chatMessage()
                .content(content)
                .senderId(senderId)
                .chatRoom(chatRoom)
                .create();
    }
}
