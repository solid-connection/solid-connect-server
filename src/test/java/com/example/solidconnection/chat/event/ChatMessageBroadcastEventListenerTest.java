package com.example.solidconnection.chat.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.solidconnection.chat.domain.MessageType;
import com.example.solidconnection.chat.dto.ChatMessageSendResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

@DisplayName("채팅 메시지 방송 이벤트 리스너 테스트")
class ChatMessageBroadcastEventListenerTest {

    private SimpMessageSendingOperations simpMessageSendingOperations;
    private ChatMessageBroadcastEventListener listener;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        simpMessageSendingOperations = mock(SimpMessageSendingOperations.class);
        listener = new ChatMessageBroadcastEventListener(simpMessageSendingOperations);

        logger = (Logger) LoggerFactory.getLogger(ChatMessageBroadcastEventListener.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void 방송에_실패하면_예외를_전파하지_않고_로그를_남긴다() {
        // given
        long roomId = 1L;
        long messageId = 2L;
        ChatMessageSendResponse response = new ChatMessageSendResponse(
                messageId,
                "메시지",
                3L,
                MessageType.TEXT,
                List.of()
        );
        ChatMessageBroadcastEvent event = new ChatMessageBroadcastEvent(roomId, response);
        RuntimeException exception = new RuntimeException("broadcast failed");
        BDDMockito.willThrow(exception)
                .given(simpMessageSendingOperations)
                .convertAndSend("/topic/chat/" + roomId, response);

        // when & then
        assertThatCode(() -> listener.broadcast(event)).doesNotThrowAnyException();

        ILoggingEvent logEvent = listAppender.list.stream()
                .filter(eventLog -> eventLog.getFormattedMessage().contains("채팅 메시지를 방송하지 못했습니다."))
                .findFirst()
                .orElseThrow();
        assertAll(
                () -> assertThat(logEvent.getLevel().toString()).isEqualTo("ERROR"),
                () -> assertThat(logEvent.getFormattedMessage()).contains("roomId=1", "messageId=2"),
                () -> assertThat(logEvent.getThrowableProxy()).isNotNull(),
                () -> assertThat(logEvent.getThrowableProxy().getMessage()).isEqualTo("broadcast failed")
        );
    }
}
