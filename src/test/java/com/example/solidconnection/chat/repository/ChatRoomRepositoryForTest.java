package com.example.solidconnection.chat.repository;

import com.example.solidconnection.chat.domain.ChatRoom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepositoryForTest extends JpaRepository<ChatRoom, Long> {

    @Query("""
           SELECT DISTINCT cr FROM ChatRoom cr
           LEFT JOIN FETCH cr.chatParticipants cp
           WHERE cr.isGroup = false
           AND EXISTS (
               SELECT 1 FROM ChatParticipant cp1 
               WHERE cp1.chatRoom = cr AND cp1.siteUserId = :mentorId
           )
           AND EXISTS (
               SELECT 1 FROM ChatParticipant cp2 
               WHERE cp2.chatRoom = cr AND cp2.siteUserId = :menteeId
           )
           AND (
               SELECT COUNT(cp3) FROM ChatParticipant cp3 
               WHERE cp3.chatRoom = cr
           ) = 2
           """)
    Optional<ChatRoom> findOneOnOneChatRoomByParticipants(@Param("mentorId") long mentorId, @Param("menteeId") long menteeId);
}
