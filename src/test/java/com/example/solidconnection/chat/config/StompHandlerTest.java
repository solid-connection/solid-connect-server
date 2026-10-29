package com.example.solidconnection.chat.config;

import static com.example.solidconnection.common.exception.ErrorCode.INVALID_INPUT;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.solidconnection.chat.service.ChatService;
import com.example.solidconnection.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

@DisplayName("STOMP 메시지 인터셉터 테스트")
class StompHandlerTest {

    @Nested
    @DisplayName("채팅방 구독")
    class 채팅방을_구독한다 {

        @Test
        void 구독_ID가_없는_SUBSCRIBE는_거부한다() {
            // given
            ChatService chatService = org.mockito.Mockito.mock(ChatService.class);
            MessageChannel messageChannel = org.mockito.Mockito.mock(MessageChannel.class);
            StompHandler stompHandler = new StompHandler(chatService);
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
            accessor.setUser(() -> "user");
            accessor.setDestination("/topic/chat/1");
            Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            // when & then
            assertThatThrownBy(() -> stompHandler.preSend(message, messageChannel))
                    .isInstanceOfSatisfying(CustomException.class,
                            exception -> org.assertj.core.api.Assertions.assertThat(exception.getErrorCode())
                                    .isEqualTo(INVALID_INPUT));
        }
    }
}
