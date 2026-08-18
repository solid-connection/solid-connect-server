package com.example.solidconnection.common.discord;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableAsync
public class DiscordNotifier {

    private static final String ADMIN_PAGE_URL = "https://admins.solid-connection.com";

    private final DiscordWebhookSender discordWebhookSender;

    @Value("${discord.webhook-url:}")
    private String webhookUrl;

    @Value("${spring.profiles.active:}")
    private String environment;

    @Async
    public void notify(DiscordNotificationType type, String applicantInfo) {
        if (webhookUrl.isBlank()) {
            return;
        }
        discordWebhookSender.send(webhookUrl, buildMessage(type, applicantInfo));
    }

    private String buildMessage(DiscordNotificationType type, String applicantInfo) {
        return "[%s] %s 검수 요청이 등록되었습니다.\n신청자: %s\n관리자 페이지: %s"
                .formatted(environment.toUpperCase(), type.getDisplayName(), applicantInfo, ADMIN_PAGE_URL);
    }
}
