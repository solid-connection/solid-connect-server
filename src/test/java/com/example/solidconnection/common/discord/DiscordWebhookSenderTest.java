package com.example.solidconnection.common.discord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@DisplayName("DiscordWebhookSender 테스트")
class DiscordWebhookSenderTest {

    private static final String WEBHOOK_URL = "https://discord.test/webhooks/channel";
    private static final String CONTENT = "백업 실패 알림";
    private static final String ROLE_ID = "1234567890";

    private RestTemplate restTemplate;
    private DiscordWebhookSender discordWebhookSender;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        discordWebhookSender = new DiscordWebhookSender(restTemplate);
    }

    private HttpEntity<Map<String, Object>> 전송된_요청() {
        ArgumentCaptor<HttpEntity<Map<String, Object>>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq(WEBHOOK_URL), requestCaptor.capture(), eq(Void.class));
        return requestCaptor.getValue();
    }

    @Test
    void 디스코드_메시지_식별자_확보를_위해_wait_true로_전송한다() {
        // given
        String waitUrl = WEBHOOK_URL + "?wait=true";
        DiscordMessageResponse expected = new DiscordMessageResponse("message-id", "channel-id");
        when(restTemplate.postForObject(eq(waitUrl), any(), eq(DiscordMessageResponse.class)))
                .thenReturn(expected);

        // when
        DiscordMessageResponse response = discordWebhookSender.sendAndGetMessage(WEBHOOK_URL, CONTENT);

        // then
        assertThat(response).isEqualTo(expected);
    }

    @Nested
    @DisplayName("메시지 전송")
    class 메시지를_전송한다 {

        @Test
        void 전달받은_webhook_url_로_content_를_전송한다() {
            // when
            boolean isSent = discordWebhookSender.send(WEBHOOK_URL, CONTENT);

            // then
            HttpEntity<Map<String, Object>> request = 전송된_요청();
            assertAll(
                    () -> assertThat(isSent).isTrue(),
                    () -> assertThat(request.getBody()).containsEntry("content", CONTENT),
                    () -> assertThat(request.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON)
            );
        }

        @Test
        void webhook_url_이_없으면_전송을_시도하지_않고_실패를_반환한다() {
            // when
            boolean nullUrlResult = discordWebhookSender.send(null, CONTENT);
            boolean emptyUrlResult = discordWebhookSender.send("", CONTENT);
            boolean blankUrlResult = discordWebhookSender.send("   ", CONTENT);

            // then
            assertAll(
                    () -> assertThat(nullUrlResult).isFalse(),
                    () -> assertThat(emptyUrlResult).isFalse(),
                    () -> assertThat(blankUrlResult).isFalse(),
                    () -> verify(restTemplate, never()).postForEntity(anyString(), any(), eq(Void.class))
            );
        }

        @Test
        void 전송이_실패하면_예외를_전파하지_않고_실패를_반환한다() {
            // given
            when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                    .thenThrow(new RestClientException("discord unavailable"));

            // when
            boolean isSent = discordWebhookSender.send(WEBHOOK_URL, CONTENT);

            // then
            assertThat(isSent).isFalse();
        }
    }

    @Nested
    @DisplayName("멘션 허용 범위")
    class 멘션_허용_범위를_제한한다 {

        @Test
        void 역할을_지정하지_않으면_모든_멘션을_차단한다() {
            // when
            discordWebhookSender.send(WEBHOOK_URL, CONTENT);

            // then
            assertThat(전송된_요청().getBody())
                    .containsEntry("allowed_mentions", Map.of("parse", List.of(), "roles", List.of()));
        }

        @Test
        void 지정한_역할만_멘션을_허용한다() {
            // when
            discordWebhookSender.send(WEBHOOK_URL, CONTENT, List.of(ROLE_ID));

            // then
            assertThat(전송된_요청().getBody())
                    .containsEntry("allowed_mentions", Map.of("parse", List.of(), "roles", List.of(ROLE_ID)));
        }

        @Test
        void everyone_문자열이_섞여도_parse_가_비어_있어_채널_전체를_호출하지_않는다() {
            // when
            discordWebhookSender.send(WEBHOOK_URL, "@everyone 백업 실패", List.of(ROLE_ID));

            // then
            @SuppressWarnings("unchecked")
            Map<String, Object> allowedMentions = (Map<String, Object>) 전송된_요청().getBody().get("allowed_mentions");
            assertThat(allowedMentions.get("parse")).isEqualTo(List.of());
        }
    }
}
