package com.example.solidconnection.chat.domain;

import com.example.solidconnection.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    private long senderId;

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom chatRoom;

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL)
    private final List<ChatAttachment> chatAttachments = new ArrayList<>();

    public ChatMessage(String content, long senderId, ChatRoom chatRoom) {
        this.content = content;
        this.senderId = senderId;
        this.chatRoom = chatRoom;
        if (chatRoom != null) {
            chatRoom.getChatMessages().add(this);
        }
    }
}
