package com.example.solidconnection.chat.fixture;

import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.domain.ChatRoom;
import com.example.solidconnection.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ChatMessageFixtureBuilder {

    private final ChatMessageRepository chatMessageRepository;

    private String content;
    private long senderId;
    private ChatRoom chatRoom;

    public ChatMessageFixtureBuilder chatMessage() {
        return new ChatMessageFixtureBuilder(chatMessageRepository);
    }

    public ChatMessageFixtureBuilder content(String content) {
        this.content = content;
        return this;
    }

    public ChatMessageFixtureBuilder senderId(long senderId) {
        this.senderId = senderId;
        return this;
    }

    public ChatMessageFixtureBuilder chatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        return this;
    }

    public ChatMessage create() {
        ChatMessage chatMessage = new ChatMessage(content, senderId, chatRoom);
        return chatMessageRepository.save(chatMessage);
    }
}
