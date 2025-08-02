package com.example.solidconnection.chat.config;

import static com.example.solidconnection.common.exception.ErrorCode.AUTHENTICATION_FAILED;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import java.security.Principal;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class StompHandler implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Principal user = accessor.getUser();
            if (user == null) {
                throw new CustomException(AUTHENTICATION_FAILED);
            }
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            SiteUserPrincipal user = (SiteUserPrincipal) accessor.getUser();
            if (user == null) {
                throw new CustomException(AUTHENTICATION_FAILED);
            }

            String destination = accessor.getDestination();
            String roomId = extractRoomId(destination);

            // todo: roomId와 user.getId() 기반으로 실제 구독 권한 검사 로직
        }

        return message;
    }

    private String extractRoomId(String destination) {
        if (destination == null) {
            throw new CustomException(ErrorCode.INVALID_ROOM_ID);
        }
        String[] parts = destination.split("/");
        if (parts.length < 4 || !parts[1].equals("topic") || !parts[2].equals("chat")) {
            throw new CustomException(ErrorCode.INVALID_ROOM_ID);
        }
        return parts[3];
    }
}
