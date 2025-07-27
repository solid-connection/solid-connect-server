package com.example.solidconnection.chat.fixture;

import com.example.solidconnection.chat.domain.ChatParticipant;
import com.example.solidconnection.chat.domain.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ChatParticipantFixture {

    private final ChatParticipantFixtureBuilder chatParticipantFixtureBuilder;

    public ChatParticipant 참여자(long siteUserId, ChatRoom chatRoom) {
        return chatParticipantFixtureBuilder.chatParticipant()
                .siteUserId(siteUserId)
                .chatRoom(chatRoom)
                .create();
    }
}
