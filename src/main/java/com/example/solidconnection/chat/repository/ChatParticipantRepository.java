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

    void deleteAllBySiteUserId(long siteUserId);

    @Query("SELECT cp.id FROM ChatParticipant cp WHERE cp.siteUserId = :siteUserId")
    List<Long> findAllIdsBySiteUserId(@Param("siteUserId") long siteUserId);
}
