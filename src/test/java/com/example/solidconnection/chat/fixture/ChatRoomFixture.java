package com.example.solidconnection.chat.fixture;

import com.example.solidconnection.chat.domain.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ChatRoomFixture {

    private final ChatRoomFixtureBuilder chatRoomFixtureBuilder;

    public ChatRoom 채팅방(boolean isGroup) {
        return chatRoomFixtureBuilder.chatRoom()
                .isGroup(isGroup)
                .create();
    }

    public ChatRoom 멘토링_채팅방(long mentoringId) {
        return chatRoomFixtureBuilder.chatRoom()
                .mentoringId(mentoringId)
                .isGroup(false)
                .create();
    }
}
