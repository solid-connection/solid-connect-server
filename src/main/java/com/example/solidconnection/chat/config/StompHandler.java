package com.example.solidconnection.chat.config;

import static com.example.solidconnection.common.exception.ErrorCode.AUTHENTICATION_FAILED;

import com.example.solidconnection.chat.service.ChatService;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.security.authentication.TokenAuthentication;
import com.example.solidconnection.security.userdetails.SiteUserDetails;
import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private static final Pattern ROOM_ID_PATTERN = Pattern.compile("^/topic/chat/(\\d+)$");
    private final ChatService chatService;

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
            Principal user = accessor.getUser();
            if (user == null) {
                throw new CustomException(AUTHENTICATION_FAILED);
            }

            TokenAuthentication tokenAuthentication = (TokenAuthentication) user;
            SiteUserDetails siteUserDetails = (SiteUserDetails) tokenAuthentication.getPrincipal();

            String destination = accessor.getDestination();
            long roomId = Long.parseLong(extractRoomId(destination));

            chatService.validateChatRoomParticipant(siteUserDetails.getSiteUser().getId(), roomId);
        }

        return message;
    }

    private String extractRoomId(String destination) {
        if (destination == null) {
            throw new CustomException(ErrorCode.INVALID_ROOM_ID);
        }

        Matcher matcher = ROOM_ID_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            throw new CustomException(ErrorCode.INVALID_ROOM_ID);
        }

        return matcher.group(1);
    }
}
