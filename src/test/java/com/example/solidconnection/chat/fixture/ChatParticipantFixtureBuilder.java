package com.example.solidconnection.chat.fixture;

import com.example.solidconnection.chat.domain.ChatParticipant;
import com.example.solidconnection.chat.domain.ChatRoom;
import com.example.solidconnection.chat.repository.ChatParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ChatParticipantFixtureBuilder {

    private final ChatParticipantRepository chatParticipantRepository;

    private long siteUserId;
    private ChatRoom chatRoom;

    public ChatParticipantFixtureBuilder chatParticipant() {
        return new ChatParticipantFixtureBuilder(chatParticipantRepository);
    }

    public ChatParticipantFixtureBuilder siteUserId(long siteUserId) {
        this.siteUserId = siteUserId;
        return this;
    }

    public ChatParticipantFixtureBuilder chatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        return this;
    }

    public ChatParticipant create() {
        ChatParticipant chatParticipant = new ChatParticipant(siteUserId, chatRoom);
        return chatParticipantRepository.save(chatParticipant);
    }
}
