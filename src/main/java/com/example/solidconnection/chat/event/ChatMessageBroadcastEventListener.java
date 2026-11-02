package com.example.solidconnection.chat.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageBroadcastEventListener {

    private final SimpMessageSendingOperations simpMessageSendingOperations;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void broadcast(ChatMessageBroadcastEvent event) {
        try {
            simpMessageSendingOperations.convertAndSend(
                    "/topic/chat/" + event.roomId(),
                    event.response()
            );
        } catch (RuntimeException e) {
            log.error("채팅 메시지를 방송하지 못했습니다. roomId={}, messageId={}",
                    event.roomId(), event.response().messageId(), e);
        }
    }
}
