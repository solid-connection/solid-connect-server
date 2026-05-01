package com.example.solidconnection.chat.domain;

import com.example.solidconnection.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    @BatchSize(size = 10)
    private final List<ChatParticipant> chatParticipants = new ArrayList<>();
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "is_group")
    private boolean isGroup = false;
    @Column(name = "mentoring_id", unique = true)
    private Long mentoringId;

    public ChatRoom(boolean isGroup) {
        this.isGroup = isGroup;
    }

    public ChatRoom(Long mentoringId, boolean isGroup) {
        this.mentoringId = mentoringId;
        this.isGroup = isGroup;
    }
}
