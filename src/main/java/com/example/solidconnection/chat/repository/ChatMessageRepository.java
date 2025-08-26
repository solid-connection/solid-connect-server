package com.example.solidconnection.chat.repository;

import com.example.solidconnection.chat.domain.ChatMessage;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
           SELECT cm FROM ChatMessage cm
           LEFT JOIN FETCH cm.chatAttachments
           WHERE cm.chatRoom.id = :roomId
           ORDER BY cm.createdAt DESC
           """)
    Slice<ChatMessage> findByRoomIdWithPaging(@Param("roomId") long roomId, Pageable pageable);

    Optional<ChatMessage> findFirstByChatRoomIdOrderByCreatedAtDesc(long chatRoomId);
}
