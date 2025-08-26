package com.example.solidconnection.cache;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheUpdateListener implements MessageListener {

    private final CompletableFutureManager futureManager;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8).replaceAll("^\"|\"$", "");
        futureManager.completeFuture(messageBody);
    }
}
