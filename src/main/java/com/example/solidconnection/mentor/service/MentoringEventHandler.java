package com.example.solidconnection.mentor.service;

import com.example.solidconnection.chat.service.ChatService;
import com.example.solidconnection.mentor.dto.MentoringApprovedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MentoringEventHandler {

    private final ChatService chatService;

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleMentoringApproved(MentoringApprovedEvent event) {
        chatService.createMentoringChatRoom(event.mentoringId(), event.mentorId(), event.menteeId());
    }
}
