package com.example.solidconnection.common.discord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class DiscordReactionClientTest {

    @Test
    void 승인_이모지를_봇_권한으로_추가한다() {
        // given
        RestTemplate restTemplate = mock(RestTemplate.class);
        DiscordReactionClient discordReactionClient = new DiscordReactionClient(restTemplate);
        ReflectionTestUtils.setField(discordReactionClient, "botToken", "bot-token");
        ArgumentCaptor<HttpEntity<Void>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        // when
        discordReactionClient.addReaction(
                "channel-id",
                "message-id",
                DiscordReactionEmoji.APPROVED.getValue()
        );

        // then
        verify(restTemplate).exchange(
                eq("https://discord.com/api/v10/channels/channel-id/messages/message-id/reactions/%E2%9C%85/@me"),
                eq(HttpMethod.PUT),
                requestCaptor.capture(),
                eq(Void.class)
        );
        assertThat(requestCaptor.getValue().getHeaders().getFirst("Authorization")).isEqualTo("Bot bot-token");
    }
}
