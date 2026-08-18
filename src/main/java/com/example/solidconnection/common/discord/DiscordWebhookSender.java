package com.example.solidconnection.common.discord;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/*
 * - Discord Webhook 으로 메시지를 전송한다.
 * - 알림 전송 실패가 호출한 기능을 실패시키지 않도록 예외를 격리하고, 전송 여부를 반환해 후속 처리를 맡긴다.
 * - 채널별로 webhook url 이 다르므로 url 을 인자로 받는다.
 * */
@Component
@RequiredArgsConstructor
@Slf4j
public class DiscordWebhookSender {

    private final RestTemplate restTemplate;

    public boolean send(String webhookUrl, String content) {
        return send(webhookUrl, content, List.of());
    }

    /*
     * - mentionableRoleIds 에 지정한 역할만 멘션할 수 있다.
     * - @everyone 과 @here 는 항상 차단되므로 content 에 섞여 들어와도 채널 전체를 호출하지 않는다.
     * */
    public boolean send(String webhookUrl, String content, List<String> mentionableRoleIds) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.error("Discord webhook url 이 설정되지 않아 알림을 전송하지 못했습니다.");
            return false;
        }
        try {
            restTemplate.postForEntity(webhookUrl, buildRequest(content, mentionableRoleIds), Void.class);
            return true;
        } catch (Exception e) {
            log.error("Discord 알림 전송에 실패했습니다.", e);
            return false;
        }
    }

    private HttpEntity<Map<String, Object>> buildRequest(String content, List<String> mentionableRoleIds) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of(
                "content", content,
                "allowed_mentions", Map.of(
                        "parse", List.of(),
                        "roles", mentionableRoleIds
                )
        );
        return new HttpEntity<>(body, headers);
    }
}
