package com.example.solidconnection.chat.config;

import static com.example.solidconnection.common.exception.ErrorCode.AUTHENTICATION_FAILED;

import com.example.solidconnection.auth.token.JwtTokenProvider;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Claims claims = validateAndExtractClaims(accessor, AUTHENTICATION_FAILED);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Claims claims = validateAndExtractClaims(accessor, AUTHENTICATION_FAILED);

            String email = claims.getSubject();
            String destination = accessor.getDestination();

            String roomId = extractRoomId(destination);

            // todo: roomId 기반 실제 구독 권한 검사 로직 추가
        }

        return message;
    }

    private Claims validateAndExtractClaims(StompHeaderAccessor accessor, ErrorCode errorCode) {
        String bearerToken = accessor.getFirstNativeHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new CustomException(errorCode);
        }
        String token = bearerToken.substring(7);
        return jwtTokenProvider.parseClaims(token);
    }

    private String extractRoomId(String destination) {
        if (destination == null) {
            throw new CustomException(ErrorCode.INVALID_ROOM_ID);
        }
        String[] parts = destination.split("/");
        if (parts.length < 3 || !parts[1].equals("topic")) {
            throw new CustomException(ErrorCode.INVALID_ROOM_ID);
        }
        return parts[2];
    }
}
