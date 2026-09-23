package com.example.solidconnection.chat.repository;

import com.example.solidconnection.chat.domain.ChatParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    boolean existsByChatRoomIdAndSiteUserId(long chatRoomId, long siteUserId);

    Optional<ChatParticipant> findByChatRoomIdAndSiteUserId(long chatRoomId, long siteUserId);

    void deleteAllByChatRoomIdIn(List<Long> chatRoomIds);

    @Query("SELECT cp.chatRoom.id FROM ChatParticipant cp WHERE cp.siteUserId = :siteUserId")
    List<Long> findAllChatRoomIdsBySiteUserId(@Param("siteUserId") long siteUserId);
}
