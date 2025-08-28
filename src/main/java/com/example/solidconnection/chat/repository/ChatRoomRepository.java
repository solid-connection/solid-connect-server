package com.example.solidconnection.chat.repository;

import com.example.solidconnection.chat.domain.ChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
           SELECT DISTINCT cr FROM ChatRoom cr
           JOIN FETCH cr.chatParticipants
           WHERE cr.id IN (
               SELECT DISTINCT cp2.chatRoom.id
               FROM ChatParticipant cp2
               WHERE cp2.siteUserId = :userId
           )
           AND cr.isGroup = false
           ORDER BY (
               SELECT MAX(cm.createdAt)
               FROM ChatMessage cm
               WHERE cm.chatRoom = cr
           ) DESC NULLS LAST
           """)
    List<ChatRoom> findOneOnOneChatRoomsByUserIdWithParticipants(@Param("userId") long userId);

    ChatRoom findByMentoringId(long mentoringId);

    List<ChatRoom> findAllByMentoringIdIn(List<Long> mentoringIds);
}
