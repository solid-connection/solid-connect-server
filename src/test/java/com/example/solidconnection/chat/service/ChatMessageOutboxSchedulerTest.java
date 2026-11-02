package com.example.solidconnection.chat.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.solidconnection.chat.config.ChatMessageOutboxProperties;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@DisplayName("채팅 메시지 Outbox 스케줄러 테스트")
class ChatMessageOutboxSchedulerTest {

    @Test
    void 오래된_처리를_회수한_뒤_전송_가능한_Outbox를_배치로_처리한다() {
        // given
        ChatMessageOutboxTransactionService transactionService =
                Mockito.mock(ChatMessageOutboxTransactionService.class);
        ChatMessageOutboxDispatcher dispatcher = Mockito.mock(ChatMessageOutboxDispatcher.class);
        ChatMessageOutboxProperties properties = new ChatMessageOutboxProperties();
        properties.setProcessingTimeout(Duration.ofMinutes(2));
        given(transactionService.findDispatchableIds(Mockito.any(ZonedDateTime.class)))
                .willReturn(List.of(1L, 2L));
        ChatMessageOutboxScheduler scheduler =
                new ChatMessageOutboxScheduler(transactionService, dispatcher, properties);

        // when
        scheduler.dispatchPendingMessages();

        // then
        ArgumentCaptor<ZonedDateTime> cutoffCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
        ArgumentCaptor<ZonedDateTime> nowCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
        then(transactionService).should().recoverStaleProcessing(cutoffCaptor.capture(), nowCaptor.capture());
        then(transactionService).should().findDispatchableIds(nowCaptor.getValue());
        then(dispatcher).should().dispatch(1L);
        then(dispatcher).should().dispatch(2L);
        org.assertj.core.api.Assertions.assertThat(Duration.between(
                cutoffCaptor.getValue(),
                nowCaptor.getValue()
        )).isEqualTo(Duration.ofMinutes(2));
    }
}
