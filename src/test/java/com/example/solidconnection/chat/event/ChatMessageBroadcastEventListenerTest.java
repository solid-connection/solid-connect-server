package com.example.solidconnection.chat.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

import com.example.solidconnection.chat.service.ChatMessageOutboxDispatcher;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.annotation.Async;

@DisplayName("채팅 메시지 방송 이벤트 리스너 테스트")
class ChatMessageBroadcastEventListenerTest {

    @Test
    void 송신_트랜잭션과_분리된_전용_실행기에서_디스패치한다() throws NoSuchMethodException {
        // given
        Method broadcast = ChatMessageBroadcastEventListener.class
                .getDeclaredMethod("broadcast", ChatMessageBroadcastEvent.class);

        // when
        Async async = broadcast.getAnnotation(Async.class);

        // then
        assertThat(async).isNotNull();
        assertThat(async.value()).isEqualTo("chatMessageOutboxExecutor");
    }

    @Test
    void Outbox_식별자를_디스패처에_전달한다() {
        // given
        ChatMessageOutboxDispatcher dispatcher = Mockito.mock(ChatMessageOutboxDispatcher.class);
        ChatMessageBroadcastEventListener listener = new ChatMessageBroadcastEventListener(dispatcher);
        ChatMessageBroadcastEvent event = new ChatMessageBroadcastEvent(1L);

        // when
        listener.broadcast(event);

        // then
        then(dispatcher).should().dispatch(1L);
    }
}
