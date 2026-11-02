package com.example.solidconnection.chat.fixture;

import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.domain.ChatMessageOutbox;
import com.example.solidconnection.chat.domain.ChatParticipant;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ChatMessageOutboxFixture {

    private final ChatMessageOutboxFixtureBuilder chatMessageOutboxFixtureBuilder;

    public ChatMessageOutbox 아웃박스(
            ChatMessage chatMessage,
            ChatParticipant recipient,
            ZonedDateTime nextAttemptAt
    ) {
        return chatMessageOutboxFixtureBuilder.chatMessageOutbox()
                .chatMessage(chatMessage)
                .recipient(recipient)
                .nextAttemptAt(nextAttemptAt)
                .create();
    }
}
