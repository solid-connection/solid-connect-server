package com.example.solidconnection.chat.event;

import static org.mockito.BDDMockito.then;

import com.example.solidconnection.chat.service.ChatMessageOutboxDispatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("채팅 메시지 방송 이벤트 리스너 테스트")
class ChatMessageBroadcastEventListenerTest {

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
