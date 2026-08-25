package com.example.solidconnection.common.discord;

import com.example.solidconnection.common.discord.service.DiscordNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableAsync
public class DiscordNotifier {

    private static final String ADMIN_PAGE_URL = "https://www.admins.solid-connection.com";

    private final DiscordWebhookSender discordWebhookSender;
    private final DiscordNotificationService discordNotificationService;
    private final DiscordReactionClient discordReactionClient;

    @Value("${discord.webhook-url:}")
    private String webhookUrl;

    @Value("${spring.profiles.active:}")
    private String environment;

    @Async
    public void notify(DiscordNotificationType type, String applicantInfo) {
        if (webhookUrl.isBlank() || "local".equalsIgnoreCase(environment)) {
            return;
        }
        discordWebhookSender.send(webhookUrl, buildMessage(type, applicantInfo));
    }

    public void notify(DiscordNotificationType type, long reviewId, String applicantInfo) {
        if (webhookUrl.isBlank() || "local".equalsIgnoreCase(environment)) {
            return;
        }
        DiscordMessageResponse response = discordWebhookSender.sendAndGetMessage(
                webhookUrl,
                buildMessage(type, applicantInfo)
        );
        discordNotificationService.save(type, reviewId, response.channelId(), response.id());
    }

    public void addReaction(DiscordNotificationType type, long reviewId, String emoji) {
        discordNotificationService.findByReviewTypeAndReviewId(type, reviewId)
                .ifPresent(message -> discordReactionClient.addReaction(
                        message.getDiscordChannelId(),
                        message.getDiscordMessageId(),
                        emoji
                ));
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
