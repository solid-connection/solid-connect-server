package com.example.solidconnection.common.discord;

import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

@Component
public class DiscordReactionClient {

    private static final String REACTION_URL =
            "https://discord.com/api/v10/channels/%s/messages/%s/reactions/%s/@me";

    private final RestTemplate discordBotRestTemplate;

    @Value("${discord.bot-token:}")
    private String botToken;

    public DiscordReactionClient(@Qualifier("discordBotRestTemplate") RestTemplate discordBotRestTemplate) {
        this.discordBotRestTemplate = discordBotRestTemplate;
    }

    public void addReaction(String channelId, String messageId, String emoji) {
        String encodedEmoji = UriUtils.encodePathSegment(emoji, StandardCharsets.UTF_8);
        String url = REACTION_URL.formatted(channelId, messageId, encodedEmoji);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bot " + botToken);
        discordBotRestTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(headers), Void.class);
    }
}
