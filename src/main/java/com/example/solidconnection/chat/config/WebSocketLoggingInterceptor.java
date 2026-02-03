package com.example.solidconnection.chat.config;

import com.example.solidconnection.security.authentication.TokenAuthentication;
import com.example.solidconnection.security.userdetails.SiteUserDetails;
import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
public class WebSocketLoggingInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (command != null) {
            Long userId = extractUserId(accessor);
            String destination = accessor.getDestination();
            logStompMessage(command, destination, userId);
        }

        return message;
    }

    private void logStompMessage(StompCommand command, String destination, Long userId) {
        switch (command) {
            case CONNECT -> log.info("[WEBSOCKET] CONNECT userId = {}", userId);
            case SUBSCRIBE -> log.info("[WEBSOCKET] SUBSCRIBE {} userId = {}", destination, userId);
            case SEND -> log.info("[WEBSOCKET] SEND {} userId = {}", destination, userId);
            case DISCONNECT -> log.info("[WEBSOCKET] DISCONNECT userId = {}", userId);
            default -> {
            }
        }
    }

    private Long extractUserId(StompHeaderAccessor accessor) {
        Principal user = accessor.getUser();
        if (user instanceof TokenAuthentication tokenAuth) {
            Object principal = tokenAuth.getPrincipal();
            if (principal instanceof SiteUserDetails details) {
                return details.getSiteUser().getId();
            }
        }
        return null;
    }
}
