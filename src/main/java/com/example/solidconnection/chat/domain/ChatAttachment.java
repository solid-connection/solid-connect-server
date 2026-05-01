package com.example.solidconnection.chat.domain;

import com.example.solidconnection.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatAttachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "is_image", nullable = false)
    private Boolean isImage;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ChatMessage chatMessage;

    public ChatAttachment(boolean isImage, String url, String thumbnailUrl, ChatMessage chatMessage) {
        this.isImage = isImage;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.chatMessage = chatMessage;
        if (chatMessage != null) {
            chatMessage.getChatAttachments().add(this);
        }
    }

    protected void setChatMessage(ChatMessage chatMessage) {
        if (this.chatMessage == chatMessage) {
            return;
        }

        if (this.chatMessage != null) {
            this.chatMessage.getChatAttachments().remove(this);
        }

        this.chatMessage = chatMessage;
        if (chatMessage != null && !chatMessage.getChatAttachments().contains(this)) {
            chatMessage.getChatAttachments().add(this);
        }
    }
}
