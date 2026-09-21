package com.example.solidconnection.chat.repository;

import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.dto.UnreadCountDto;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 1차 쿼리 개선(2026-09-21, 미커밋 로컬 검증용):
    // LEFT JOIN FETCH로 chatAttachments(1:N)를 같이 가져오면서 Pageable을 쓰면, Hibernate가
    // "firstResult/maxResults specified with collection fetch; applying in memory" 규칙 때문에
    // SQL에 LIMIT/OFFSET을 넣지 못하고 해당 방의 메시지를 전부 로드한 뒤 Java에서 페이징한다.
    // 즉 지금까지 이 메서드는 무한스크롤 페이지 하나를 요청할 때마다 방의 전체 메시지를 로드하고 있었다.
    // chatAttachments는 필터/정렬에 쓰이지 않으므로 fetch join을 제거해서 LIMIT이 SQL에 그대로 전달되게 하고,
    // ChatMessage.chatAttachments에 @BatchSize를 추가해 나중에 지연 로딩될 때 배치 조회되게 위임했다.
    @Query("""
           SELECT cm FROM ChatMessage cm
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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
                   UPDATE chat_message cm SET cm.is_deleted = :isDeleted
                   WHERE cm.id IN (SELECT r.target_id FROM report r WHERE r.target_type = 'CHAT')
                   AND cm.sender_id IN (SELECT cp.id FROM chat_participant cp WHERE cp.site_user_id = :siteUserId)
                   """, nativeQuery = true)
    void updateReportedChatMessagesIsDeleted(@Param("siteUserId") long siteUserId, @Param("isDeleted") boolean isDeleted);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
                   UPDATE chat_message cm SET cm.is_deleted = :isDeleted
                   WHERE cm.id IN (SELECT r.target_id FROM report r WHERE r.target_type = 'CHAT')
                   AND cm.sender_id IN (SELECT cp.id FROM chat_participant cp WHERE cp.site_user_id IN :siteUserIds)
                   """, nativeQuery = true)
    void bulkUpdateReportedChatMessagesIsDeleted(@Param("siteUserIds") List<Long> siteUserIds, @Param("isDeleted") boolean isDeleted);
}
