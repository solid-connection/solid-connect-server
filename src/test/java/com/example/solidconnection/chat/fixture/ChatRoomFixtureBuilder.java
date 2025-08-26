package com.example.solidconnection.chat.fixture;

import com.example.solidconnection.chat.domain.ChatRoom;
import com.example.solidconnection.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ChatRoomFixtureBuilder {

    private final ChatRoomRepository chatRoomRepository;

    private boolean isGroup;
    private Long mentoringId;

    public ChatRoomFixtureBuilder chatRoom() {
        return new ChatRoomFixtureBuilder(chatRoomRepository);
    }

    public ChatRoomFixtureBuilder isGroup(boolean isGroup) {
        this.isGroup = isGroup;
        return this;
    }

    public ChatRoomFixtureBuilder mentoringId(long mentoringId) {
        this.mentoringId = mentoringId;
        return this;
    }

    public ChatRoom create() {
        ChatRoom chatRoom = new ChatRoom(mentoringId, isGroup);
        return chatRoomRepository.save(chatRoom);
    }
}
