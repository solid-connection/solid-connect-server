package com.example.solidconnection.chat.fixture;

import com.example.solidconnection.chat.domain.ChatReadStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ChatReadStatusFixture {

    private final ChatReadStatusFixtureBuilder chatReadStatusFixtureBuilder;

    public ChatReadStatus 읽음상태(long chatRoomId, long chatParticipantId) {
        return chatReadStatusFixtureBuilder.chatReadStatus()
                .chatRoomId(chatRoomId)
                .chatParticipantId(chatParticipantId)
                .create();
    }
}
