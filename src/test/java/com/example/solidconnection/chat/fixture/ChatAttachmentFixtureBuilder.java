package com.example.solidconnection.chat.fixture;

import com.example.solidconnection.chat.domain.ChatAttachment;
import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.repository.ChatAttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ChatAttachmentFixtureBuilder {

    private final ChatAttachmentRepository chatAttachmentRepository;

    private boolean isImage;
    private String url;
    private String thumbnailUrl;
    private ChatMessage chatMessage;

    public ChatAttachmentFixtureBuilder chatAttachment() {
        return new ChatAttachmentFixtureBuilder(chatAttachmentRepository);
    }

    public ChatAttachmentFixtureBuilder isImage(boolean isImage) {
        this.isImage = isImage;
        return this;
    }

    public ChatAttachmentFixtureBuilder url(String url) {
        this.url = url;
        return this;
    }

    public ChatAttachmentFixtureBuilder thumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }

    public ChatAttachmentFixtureBuilder chatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
        return this;
    }

    public ChatAttachment create() {
        ChatAttachment attachment = new ChatAttachment(isImage, url, thumbnailUrl, chatMessage);
        return chatAttachmentRepository.save(attachment);
    }
}
