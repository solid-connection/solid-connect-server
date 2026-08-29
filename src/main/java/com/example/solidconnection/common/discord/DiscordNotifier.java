package com.example.solidconnection.common.discord;

import com.example.solidconnection.common.discord.domain.DiscordNotification;
import com.example.solidconnection.common.discord.service.DiscordNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@EnableAsync
@Slf4j
public class DiscordNotifier {

    private static final String ADMIN_PAGE_URL = "https://www.admins.solid-connection.com";

    private final DiscordWebhookSender discordWebhookSender;
    private final DiscordNotificationService discordNotificationService;
    private final String webhookUrl;
    private final String environment;

    public DiscordNotifier(
            DiscordWebhookSender discordWebhookSender,
            DiscordNotificationService discordNotificationService,
            @Value("${discord.webhook-url:}") String webhookUrl,
            @Value("${spring.profiles.active:}") String environment
    ) {
        this.discordWebhookSender = discordWebhookSender;
        this.discordNotificationService = discordNotificationService;
        this.webhookUrl = webhookUrl;
        this.environment = environment;
    }

    @Async
    public void notify(DiscordNotificationType type, String applicantInfo) {
        if (isNotifyDisabled()) {
            return;
        }
        discordWebhookSender.send(webhookUrl, buildMessage(type, applicantInfo));
    }

    public void notify(DiscordNotificationType type, long reviewId, String applicantInfo) {
        if (isNotifyDisabled()) {
            return;
        }
        DiscordMessageResponse response = discordWebhookSender.sendAndGetMessage(
                webhookUrl,
                buildMessage(type, applicantInfo)
        );
        discordNotificationService.save(type, reviewId, response.channelId(), response.id());
    }

    /*
     * - 검수 결과를 알림 메시지 앞에 마커로 표시한다.
     * - 봇 권한이 필요한 반응 대신, webhook 이 보낸 메시지를 직접 편집한다.
     * - 검수 시점 데이터로 본문을 다시 만들어 통째로 덮어쓰므로 마커가 누적되지 않는다.
     * */
    public void markReviewResult(
            DiscordNotificationType type,
            long reviewId,
            String applicantInfo,
            String marker
    ) {
        if (isNotifyDisabled()) {
            return;
        }
        discordNotificationService.findByReviewTypeAndReviewId(type, reviewId)
                .ifPresent(notification -> editMessage(notification, type, applicantInfo, marker));
    }

    private void editMessage(
            DiscordNotification notification,
            DiscordNotificationType type,
            String applicantInfo,
            String marker
    ) {
        try {
            discordWebhookSender.editMessage(
                    webhookUrl,
                    notification.getDiscordMessageId(),
                    marker + " " + buildMessage(type, applicantInfo)
            );
        } catch (HttpClientErrorException.NotFound e) {
            /*
             * - 편집 대상이 사라진 경우로, 재시도해도 성공하지 않으므로 검수를 실패시키지 않는다.
             * - 디스코드는 메시지 부재(10008)와 webhook 부재(10015)를 모두 404 로 응답한다.
             *   webhook 부재는 설정 사고이므로 구분할 수 있도록 응답 본문을 남긴다.
             * - 응답 본문에는 webhook url 이 포함되지 않으므로 토큰이 노출되지 않는다.
             * */
            log.warn("검수 결과를 표시하지 못했습니다. reviewType={}, reviewId={}, discordError={}",
                    type, notification.getReviewId(), e.getResponseBodyAsString());
        }
    }

    private boolean isNotifyDisabled() {
        return webhookUrl.isBlank() || "local".equalsIgnoreCase(environment);
    }

    private String buildMessage(DiscordNotificationType type, String applicantInfo) {
        String body = "%s 검수 요청이 등록되었습니다.\n신청자: %s\n관리자 페이지: %s"
                .formatted(type.getDisplayName(), applicantInfo, ADMIN_PAGE_URL);
        return switch (environment.toLowerCase()) {
            case "prod" -> body;
            case "dev" -> "[개발 서버 알림입니다]\n" + body;
            default -> "[%s]\n%s".formatted(environment.toUpperCase(), body);
        };
    }
}
