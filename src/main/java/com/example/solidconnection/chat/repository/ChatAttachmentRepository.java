package com.example.solidconnection.chat.repository;

import com.example.solidconnection.chat.domain.ChatAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatAttachmentRepository extends JpaRepository<ChatAttachment, Long> {

}
