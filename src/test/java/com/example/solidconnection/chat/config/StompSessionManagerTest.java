package com.example.solidconnection.chat.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

@ExtendWith(MockitoExtension.class)
@DisplayName("STOMP 세션 관리자 테스트")
class StompSessionManagerTest {

    private static final String SESSION_ID = "session-id";
    private static final Duration SUBSCRIPTION_TIMEOUT = Duration.ofSeconds(30);

    @Mock
    private TaskScheduler scheduler;

    @Mock
    private ScheduledFuture<?> timeoutTask;

    @Mock
    private ScheduledFuture<?> renewedTimeoutTask;

    @Mock
    private WebSocketSession session;

    private StompSessionManager stompSessionManager;

    @BeforeEach
    void setUp() {
        StompProperties stompProperties = new StompProperties(null, null, SUBSCRIPTION_TIMEOUT);
        TaskExecutor directExecutor = Runnable::run;
        stompSessionManager = new StompSessionManager(scheduler, directExecutor, stompProperties);

        given(session.getId()).willReturn(SESSION_ID);
    }

    @Test
    void 제한시간까지_구독하지_않으면_세션을_종료한다() throws Exception {
        // given
        given(session.isOpen()).willReturn(true);
        doReturn(timeoutTask).when(scheduler).schedule(any(Runnable.class), any(Instant.class));
        stompSessionManager.registerSession(session);
        ArgumentCaptor<Runnable> timeoutActionCaptor = ArgumentCaptor.forClass(Runnable.class);
        then(scheduler).should().schedule(timeoutActionCaptor.capture(), any(Instant.class));

        // when
        timeoutActionCaptor.getValue().run();

        // then
        then(session).should().close(any(CloseStatus.class));
    }

    @Test
    void 세션_종료_실패는_타임아웃_작업으로_전파하지_않는다() throws Exception {
        // given
        given(session.isOpen()).willReturn(true);
        doReturn(timeoutTask).when(scheduler).schedule(any(Runnable.class), any(Instant.class));
        doThrow(new IllegalStateException("close failed"))
                .when(session).close(any(CloseStatus.class));
        stompSessionManager.registerSession(session);
        ArgumentCaptor<Runnable> timeoutActionCaptor = ArgumentCaptor.forClass(Runnable.class);
        then(scheduler).should().schedule(timeoutActionCaptor.capture(), any(Instant.class));

        // when & then
        assertThatCode(timeoutActionCaptor.getValue()::run).doesNotThrowAnyException();
    }

    @Test
    void 제한시간_안에_구독하면_종료_작업을_취소한다() throws Exception {
        // given
        doReturn(timeoutTask).when(scheduler).schedule(any(Runnable.class), any(Instant.class));
        stompSessionManager.registerSession(session);
        ArgumentCaptor<Runnable> timeoutActionCaptor = ArgumentCaptor.forClass(Runnable.class);
        then(scheduler).should().schedule(timeoutActionCaptor.capture(), any(Instant.class));

        // when
        stompSessionManager.completeSubscription(SESSION_ID);
        timeoutActionCaptor.getValue().run();

        // then
        then(timeoutTask).should().cancel(false);
        then(session).should(never()).close(any(CloseStatus.class));
    }

    @Test
    void STOMP_연결이_수립되면_구독_제한시간을_다시_계산하고_이전_작업은_무시한다() throws Exception {
        // given
        doReturn(timeoutTask, renewedTimeoutTask)
                .when(scheduler).schedule(any(Runnable.class), any(Instant.class));
        stompSessionManager.registerSession(session);
        ArgumentCaptor<Runnable> timeoutActionCaptor = ArgumentCaptor.forClass(Runnable.class);

        // when
        stompSessionManager.startSubscriptionTimeout(SESSION_ID);
        then(scheduler).should(times(2)).schedule(timeoutActionCaptor.capture(), any(Instant.class));
        timeoutActionCaptor.getAllValues().getFirst().run();

        // then
        then(timeoutTask).should().cancel(false);
        then(session).should(never()).close(any(CloseStatus.class));

        // when
        stompSessionManager.completeSubscription(SESSION_ID);

        // then
        then(renewedTimeoutTask).should().cancel(false);
    }

    @Test
    void 연결이_종료되면_예약_작업과_세션을_정리한다() throws Exception {
        // given
        doReturn(timeoutTask).when(scheduler).schedule(any(Runnable.class), any(Instant.class));
        stompSessionManager.registerSession(session);
        ArgumentCaptor<Runnable> timeoutActionCaptor = ArgumentCaptor.forClass(Runnable.class);
        then(scheduler).should().schedule(timeoutActionCaptor.capture(), any(Instant.class));

        // when
        stompSessionManager.removeSession(SESSION_ID);
        stompSessionManager.removeSession(SESSION_ID);
        timeoutActionCaptor.getValue().run();

        // then
        then(timeoutTask).should().cancel(false);
        then(session).should(never()).close(any(CloseStatus.class));
    }
}
