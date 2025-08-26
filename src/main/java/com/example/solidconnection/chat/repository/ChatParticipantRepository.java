package com.example.solidconnection.chat.repository;

import com.example.solidconnection.chat.domain.ChatParticipant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    boolean existsByChatRoomIdAndSiteUserId(long chatRoomId, long siteUserId);

    Optional<ChatParticipant> findByChatRoomIdAndSiteUserId(long chatRoomId, long siteUserId);
}
