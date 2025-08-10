package com.example.solidconnection.chat.repository;

import com.example.solidconnection.chat.domain.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
           SELECT cr FROM ChatRoom cr
           JOIN cr.chatParticipants cp
           WHERE cp.siteUserId = :userId AND cr.isGroup = false
           ORDER BY (
               SELECT MAX(cm.createdAt)
               FROM ChatMessage cm
               WHERE cm.chatRoom = cr
           ) DESC NULLS LAST
           """)
    List<ChatRoom> findOneOnOneChatRoomsByUserId(@Param("userId") long userId);

    @Query("""
           SELECT COUNT(cm) FROM ChatMessage cm
           LEFT JOIN ChatReadStatus crs ON crs.chatRoomId = cm.chatRoom.id 
               AND crs.chatParticipantId = (
                   SELECT cp.id FROM ChatParticipant cp 
                   WHERE cp.chatRoom.id = :chatRoomId 
                   AND cp.siteUserId = :userId
               )
           WHERE cm.chatRoom.id = :chatRoomId
           AND cm.senderId != :userId
           AND (crs.updatedAt IS NULL OR cm.createdAt > crs.updatedAt)
           """)
    long countUnreadMessages(@Param("chatRoomId") long chatRoomId, @Param("userId") long userId);

    Optional<ChatRoom> findByMentoringId(long mentoringId);
}
