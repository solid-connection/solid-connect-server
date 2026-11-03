package com.example.solidconnection.chat.fixture;

import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.domain.ChatMessageOutbox;
import com.example.solidconnection.chat.domain.ChatParticipant;
import com.example.solidconnection.chat.repository.ChatMessageOutboxRepository;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ChatMessageOutboxFixtureBuilder {

    private final ChatMessageOutboxRepository chatMessageOutboxRepository;

    private ChatMessage chatMessage;
    private ChatParticipant recipient;
    private ZonedDateTime nextAttemptAt;

    public ChatMessageOutboxFixtureBuilder chatMessageOutbox() {
        return new ChatMessageOutboxFixtureBuilder(chatMessageOutboxRepository);
    }

    public ChatMessageOutboxFixtureBuilder chatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
        return this;
    }

    public ChatMessageOutboxFixtureBuilder recipient(ChatParticipant recipient) {
        this.recipient = recipient;
        return this;
    }

    public ChatMessageOutboxFixtureBuilder nextAttemptAt(ZonedDateTime nextAttemptAt) {
        this.nextAttemptAt = nextAttemptAt;
        return this;
    }

    public ChatMessageOutbox create() {
        ChatMessageOutbox outbox = new ChatMessageOutbox(
                chatMessage,
                recipient,
                nextAttemptAt
        );
        return chatMessageOutboxRepository.save(outbox);
    }
}
