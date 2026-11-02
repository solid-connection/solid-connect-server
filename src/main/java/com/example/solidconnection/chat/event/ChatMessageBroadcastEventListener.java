package com.example.solidconnection.chat.event;

import com.example.solidconnection.chat.service.ChatMessageOutboxDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatMessageBroadcastEventListener {

    private final ChatMessageOutboxDispatcher dispatcher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void broadcast(ChatMessageBroadcastEvent event) {
        dispatcher.dispatch(event.outboxId());
    }
}
