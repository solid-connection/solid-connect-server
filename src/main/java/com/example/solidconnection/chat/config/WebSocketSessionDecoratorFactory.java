package com.example.solidconnection.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

@Component
@RequiredArgsConstructor
public class WebSocketSessionDecoratorFactory implements WebSocketHandlerDecoratorFactory {

    private final StompSessionManager stompSessionManager;

    @Override
    public WebSocketHandler decorate(WebSocketHandler handler) {
        return new WebSocketHandlerDecorator(handler) {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                stompSessionManager.registerSession(session);
                try {
                    super.afterConnectionEstablished(session);
                } catch (Exception e) {
                    stompSessionManager.removeSession(session.getId());
                    throw e;
                }
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                try {
                    super.afterConnectionClosed(session, closeStatus);
                } finally {
                    stompSessionManager.removeSession(session.getId());
                }
            }
        };
    }
}
