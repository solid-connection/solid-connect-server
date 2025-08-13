package com.example.solidconnection.mentor.service;

import com.example.solidconnection.chat.service.ChatService;
import com.example.solidconnection.mentor.dto.MentoringApprovedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MentoringEventHandler {

    private final ChatService chatService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void handleMentoringApproved(MentoringApprovedEvent event) {
        chatService.createMentoringChatRoom(event.mentoringId(), event.mentorId(), event.menteeId());
    }
}
