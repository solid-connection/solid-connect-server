package com.example.solidconnection.chat.repository;

import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.dto.UnreadCountDto;
import java.util.List;
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

    @Query("""
           SELECT cm FROM ChatMessage cm
           WHERE cm.id IN (
               SELECT MAX(cm2.id)
               FROM ChatMessage cm2
               WHERE cm2.chatRoom.id IN :chatRoomIds
               GROUP BY cm2.chatRoom.id
           )
           """)
    List<ChatMessage> findLatestMessagesByChatRoomIds(@Param("chatRoomIds") List<Long> chatRoomIds);

    @Query("""
           SELECT new com.example.solidconnection.chat.dto.UnreadCountDto(
               cm.chatRoom.id,
               COUNT(cm)
           )
           FROM ChatMessage cm
           LEFT JOIN ChatReadStatus crs ON crs.chatRoomId = cm.chatRoom.id
               AND crs.chatParticipantId = (
                   SELECT cp.id FROM ChatParticipant cp
                   WHERE cp.chatRoom.id = cm.chatRoom.id
                   AND cp.siteUserId = :userId
               )
           WHERE cm.chatRoom.id IN :chatRoomIds
           AND cm.senderId != :userId
           AND (crs.updatedAt IS NULL OR cm.createdAt > crs.updatedAt)
           GROUP BY cm.chatRoom.id
           """)
    List<UnreadCountDto> countUnreadMessagesBatch(@Param("chatRoomIds") List<Long> chatRoomIds, @Param("userId") long userId);
}
