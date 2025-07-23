package com.example.solidconnection.chat.config;

import com.example.solidconnection.auth.token.JwtTokenProvider;
import com.example.solidconnection.common.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.common.exception.ErrorCode.UNAUTHORIZED_SUBSCRIBE;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if(StompCommand.CONNECT == accessor.getCommand()){
            log.info("connect요청시 토큰 유효성 검증");

            String bearerToken = accessor.getFirstNativeHeader("Authorization");

            if(bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Authorization header missing or invalid format");
            }

            String token = bearerToken.substring(7);

            Claims claims = jwtTokenProvider.parseClaims(token);

            log.info("토큰 검증 성공, 유저 인증 완료 - 사용자: {}", claims.getSubject());
        }

        if(StompCommand.SUBSCRIBE == accessor.getCommand()){
            log.info("subscribe 검증");

            try {
                String bearerToken = accessor.getFirstNativeHeader("Authorization");

                if(bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                    throw new IllegalArgumentException("Authorization header missing or invalid format");
                }

                String token = bearerToken.substring(7);

                Claims claims = jwtTokenProvider.parseClaims(token);

                String email = claims.getSubject();
                String destination = accessor.getDestination();

                if(destination != null && destination.contains("/topic/")) {
                    String roomId = destination.split("/")[2];
                    log.info("사용자 {} 가 룸 {} 구독 시도", email, roomId);

                    // todo: room 검증로직 구현
                }

            } catch (Exception e) {
                log.error("구독 검증 실패: {}", e.getMessage());
                throw new CustomException(UNAUTHORIZED_SUBSCRIBE);
            }
        }

        return message;
    }
}
