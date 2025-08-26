package com.example.solidconnection.chat.fixture;

import com.example.solidconnection.chat.domain.ChatAttachment;
import com.example.solidconnection.chat.domain.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ChatAttachmentFixture {

    private final ChatAttachmentFixtureBuilder chatAttachmentFixtureBuilder;

    public ChatAttachment 첨부파일(boolean isImage, String url, String thumbnailUrl, ChatMessage chatMessage) {
        return chatAttachmentFixtureBuilder.chatAttachment()
                .isImage(isImage)
                .url(url)
                .thumbnailUrl(thumbnailUrl)
                .chatMessage(chatMessage)
                .create();
    }

    public ChatAttachment 이미지(String url, String thumbnailUrl, ChatMessage chatMessage) {
        return 첨부파일(true, url, thumbnailUrl, chatMessage);
    }

    public ChatAttachment 파일(String url, ChatMessage chatMessage) {
        return 첨부파일(false, url, null, chatMessage);
    }
}
