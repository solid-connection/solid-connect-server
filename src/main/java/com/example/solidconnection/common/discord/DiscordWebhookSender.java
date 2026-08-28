package com.example.solidconnection.common.discord;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

/*
 * - Discord Webhook 으로 메시지를 전송하고 편집한다.
 * - send() 만 예외를 격리하고 전송 여부를 반환한다.
 *   sendAndGetMessage() 와 editMessage() 는 호출한 기능이 결과를 알아야 하므로 예외를 전파한다.
 * - 채널별로 webhook url 이 다르므로 url 을 인자로 받는다.
 * - webhook url 은 경로에 인증 토큰을 포함하므로 로그와 메트릭에 남기지 않는다.
 * */
@Component
@Slf4j
public class DiscordWebhookSender {

    private final RestTemplate discordWebhookRestTemplate;

    public DiscordWebhookSender(@Qualifier("discordWebhookRestTemplate") RestTemplate discordWebhookRestTemplate) {
        this.discordWebhookRestTemplate = discordWebhookRestTemplate;
    }

    public boolean send(String webhookUrl, String content) {
        return send(webhookUrl, content, List.of());
    }

    /*
     * - mentionableRoleIds 에 지정한 역할만 멘션할 수 있다.
     * - everyone 과 here 멘션은 항상 차단되므로 content 에 섞여 들어와도 채널 전체를 호출하지 않는다.
     * */
    public boolean send(String webhookUrl, String content, List<String> mentionableRoleIds) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.error("Discord webhook url 이 설정되지 않아 알림을 전송하지 못했습니다.");
            return false;
        }
        try {
            discordWebhookRestTemplate.postForEntity(webhookUrl, buildRequest(content, mentionableRoleIds), Void.class);
            return true;
        } catch (RestClientResponseException e) {
            // 예외 메시지에 요청 url 이 포함되므로 상태 코드만 남긴다.
            log.error("Discord 알림 전송이 실패 응답을 받았습니다. status={}", e.getStatusCode().value());
            return false;
        } catch (Exception e) {
            log.error("Discord 알림 전송에 실패했습니다. exception={}", e.getClass().getSimpleName());
            return false;
        }
    }

    public DiscordMessageResponse sendAndGetMessage(String webhookUrl, String content) {
        String waitUrl = webhookUrl + (webhookUrl.contains("?") ? "&wait=true" : "?wait=true");
        return discordWebhookRestTemplate.postForObject(
                waitUrl,
                buildRequest(content, List.of()),
                DiscordMessageResponse.class
        );
    }

    /*
     * - webhook 이 보낸 메시지는 봇 권한 없이 webhook 토큰만으로 편집할 수 있다.
     * - PATCH 는 content 를 통째로 덮어쓴다.
     * */
    public void editMessage(String webhookUrl, String messageId, String content) {
        discordWebhookRestTemplate.exchange(
                messageUrl(webhookUrl, messageId),
                HttpMethod.PATCH,
                buildRequest(content, List.of()),
                Void.class
        );
    }

    // webhook url 에 쿼리스트링이 붙어 있을 수 있으므로 경로 뒤에 이어붙이지 않는다.
    private String messageUrl(String webhookUrl, String messageId) {
        int queryIndex = webhookUrl.indexOf('?');
        if (queryIndex < 0) {
            return webhookUrl + "/messages/" + messageId;
        }
        return webhookUrl.substring(0, queryIndex) + "/messages/" + messageId + webhookUrl.substring(queryIndex);
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
