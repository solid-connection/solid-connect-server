package com.example.solidconnection.chat.domain;

import com.example.solidconnection.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_chat_room_mentoring_id",
                columnNames = {"mentoring_id"}
        )
})
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isGroup = false;

    @Column(name = "mentoring_id")
    private Long mentoringId;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    @BatchSize(size = 10)
    private final List<ChatParticipant> chatParticipants = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private final List<ChatMessage> chatMessages = new ArrayList<>();

    public ChatRoom(boolean isGroup) {
        this.isGroup = isGroup;
    }

    public ChatRoom(Long mentoringId, boolean isGroup) {
        this.mentoringId = mentoringId;
        this.isGroup = isGroup;
    }
}
