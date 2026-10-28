package com.example.solidconnection.chat.config;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class StompSessionManager {

    private static final CloseStatus SUBSCRIPTION_TIMEOUT_STATUS = CloseStatus.POLICY_VIOLATION
            .withReason("STOMP subscription timeout");

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, SubscriptionTimeoutTask> subscriptionTimeoutTasks = new ConcurrentHashMap<>();
    private final TaskScheduler stompSubscriptionTimeoutScheduler;
    private final TaskExecutor stompSessionCloseExecutor;
    private final StompProperties stompProperties;

    public StompSessionManager(
            @Qualifier("stompSubscriptionTimeoutScheduler") TaskScheduler stompSubscriptionTimeoutScheduler,
            @Qualifier("stompSessionCloseExecutor") TaskExecutor stompSessionCloseExecutor,
            StompProperties stompProperties
    ) {
        this.stompSubscriptionTimeoutScheduler = stompSubscriptionTimeoutScheduler;
        this.stompSessionCloseExecutor = stompSessionCloseExecutor;
        this.stompProperties = stompProperties;
    }

    public void registerSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
        startSubscriptionTimeout(session.getId());
    }

    public void startSubscriptionTimeout(String sessionId) {
        Object taskToken = new Object();
        Instant timeoutAt = Instant.now().plus(stompProperties.subscriptionTimeout());
        ScheduledFuture<?> timeoutTask = stompSubscriptionTimeoutScheduler.schedule(
                () -> closeUnsubscribedSession(sessionId, taskToken),
                timeoutAt
        );
        SubscriptionTimeoutTask previousTask = subscriptionTimeoutTasks.put(
                sessionId,
                new SubscriptionTimeoutTask(taskToken, timeoutTask)
        );
        cancel(previousTask);
    }

    private void closeUnsubscribedSession(String sessionId, Object taskToken) {
        SubscriptionTimeoutTask timeoutTask = subscriptionTimeoutTasks.get(sessionId);
        if (timeoutTask == null
                || timeoutTask.token() != taskToken
                || !subscriptionTimeoutTasks.remove(sessionId, timeoutTask)) {
            return;
        }

        WebSocketSession session = sessions.remove(sessionId);
        if (session == null || !session.isOpen()) {
            return;
        }

        stompSessionCloseExecutor.execute(() -> closeSession(sessionId, session));
    }

    private void closeSession(String sessionId, WebSocketSession session) {
        try {
            session.close(SUBSCRIPTION_TIMEOUT_STATUS);
            log.warn("Closed STOMP session because no subscription was received. sessionId={}", sessionId);
        } catch (IOException | RuntimeException e) {
            log.warn("Failed to close unsubscribed STOMP session. sessionId={}", sessionId, e);
        }
    }

    public void completeSubscription(String sessionId) {
        cancel(subscriptionTimeoutTasks.remove(sessionId));
    }

    public void removeSession(String sessionId) {
        cancel(subscriptionTimeoutTasks.remove(sessionId));
        sessions.remove(sessionId);
    }

    private void cancel(SubscriptionTimeoutTask timeoutTask) {
        if (timeoutTask != null) {
            timeoutTask.future().cancel(false);
        }
    }

    private record SubscriptionTimeoutTask(
            Object token,
            ScheduledFuture<?> future
    ) {
    }
}
