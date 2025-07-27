package com.example.solidconnection.chat.repository;

import com.example.solidconnection.chat.domain.ChatReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatReadStatusRepository extends JpaRepository<ChatReadStatus, Long> {

    @Modifying
    @Query(value = """
                   INSERT INTO chat_read_status (chat_room_id, chat_participant_id, created_at, updated_at)
                   VALUES (:chatRoomId, :chatParticipantId, NOW(6), NOW(6))
                   ON DUPLICATE KEY UPDATE updated_at = NOW(6)
                   """, nativeQuery = true)
    void upsertReadStatus(@Param("chatRoomId") long chatRoomId, @Param("chatParticipantId") long chatParticipantId);
}
