package com.example.solidconnection.chat.fixture;

import com.example.solidconnection.chat.domain.ChatReadStatus;
import com.example.solidconnection.chat.repository.ChatReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ChatReadStatusFixtureBuilder {

    private final ChatReadStatusRepository chatReadStatusRepository;

    private long chatRoomId;
    private long chatParticipantId;

    public ChatReadStatusFixtureBuilder chatReadStatus() {
        return new ChatReadStatusFixtureBuilder(chatReadStatusRepository);
    }

    public ChatReadStatusFixtureBuilder chatRoomId(long chatRoomId) {
        this.chatRoomId = chatRoomId;
        return this;
    }

    public ChatReadStatusFixtureBuilder chatParticipantId(long chatParticipantId) {
        this.chatParticipantId = chatParticipantId;
        return this;
    }

    public ChatReadStatus create() {
        ChatReadStatus chatReadStatus = new ChatReadStatus(chatRoomId, chatParticipantId);
        return chatReadStatusRepository.save(chatReadStatus);
    }
}
