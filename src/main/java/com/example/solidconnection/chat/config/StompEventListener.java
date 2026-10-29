package com.example.solidconnection.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@RequiredArgsConstructor
public class StompEventListener {

    private final StompSessionManager stompSessionManager;

    @EventListener
    public void connectHandle(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        stompSessionManager.startSubscriptionTimeout(accessor.getSessionId());
    }

    @EventListener
    public void subscribeHandle(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        stompSessionManager.completeSubscription(accessor.getSessionId());
    }

    @EventListener
    public void disconnectHandle(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        stompSessionManager.removeSession(accessor.getSessionId());
    }
}
