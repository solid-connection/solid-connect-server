package com.example.solidconnection.common.discord;

import com.example.solidconnection.common.discord.event.DiscordReportCreatedEvent;
import com.example.solidconnection.common.discord.event.DiscordReviewCompletedEvent;
import com.example.solidconnection.common.discord.event.DiscordReviewRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/*
 * - 커밋 이후 별도 스레드에서 전송하므로 디스코드 장애가 업무 트랜잭션에 영향을 주지 않는다.
 * - 예외 메시지에 webhook url 이 포함될 수 있으므로 예외 클래스명만 남긴다.
 * */
@Component
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class DiscordNotificationEventListener {

    private final DiscordNotifier discordNotifier;

    @Async("asyncExecutor")
    @TransactionalEventListener
    public void handleReviewRequested(DiscordReviewRequestedEvent event) {
        try {
            discordNotifier.notify(event.type(), event.reviewId(), event.applicantInfo());
        } catch (Exception e) {
            log.error("검수 요청 알림을 전송하지 못했습니다. reviewType={}, reviewId={}, exception={}",
                    event.type(), event.reviewId(), e.getClass().getSimpleName());
        }
    }

    @Async("asyncExecutor")
    @TransactionalEventListener
    public void handleReviewCompleted(DiscordReviewCompletedEvent event) {
        try {
            discordNotifier.markReviewResult(event.type(), event.reviewId(), event.applicantInfo(), event.marker());
        } catch (Exception e) {
            log.error("검수 결과를 표시하지 못했습니다. reviewType={}, reviewId={}, exception={}",
                    event.type(), event.reviewId(), e.getClass().getSimpleName());
        }
    }

    @Async("asyncExecutor")
    @TransactionalEventListener
    public void handleReportCreated(DiscordReportCreatedEvent event) {
        discordNotifier.notify(DiscordNotificationType.REPORT, "신고자 ID: " + event.reporterId());
    }
}
