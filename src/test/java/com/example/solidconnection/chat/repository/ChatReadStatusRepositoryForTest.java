package com.example.solidconnection.chat.repository;

import com.example.solidconnection.chat.domain.ChatReadStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatReadStatusRepositoryForTest extends JpaRepository<ChatReadStatus, Long> {

    Optional<ChatReadStatus> findByChatRoomIdAndChatParticipantId(long chatRoomId, long chatParticipantId);
}
