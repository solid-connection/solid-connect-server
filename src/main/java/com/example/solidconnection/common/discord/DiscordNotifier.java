package com.example.solidconnection.common.discord;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@EnableAsync
@Slf4j
public class DiscordNotifier {

    private static final String ADMIN_PAGE_URL = "https://www.admins.solid-connection.com";

    private final RestTemplate restTemplate;

    @Value("${discord.webhook-url:}")
    private String webhookUrl;

    @Value("${spring.profiles.active:}")
    private String environment;

    @Async
    public void notify(DiscordNotificationType type, String applicantInfo) {
        if (webhookUrl.isBlank() || "local".equalsIgnoreCase(environment)) {
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of("content", buildMessage(type, applicantInfo)), headers);
            restTemplate.postForEntity(webhookUrl, request, Void.class);
        } catch (Exception e) {
            log.error("Discord 검수 알림 전송 실패. type={}, applicantInfo={}", type, applicantInfo, e);
        }
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
