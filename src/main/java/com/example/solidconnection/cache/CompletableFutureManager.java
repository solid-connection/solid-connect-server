package com.example.solidconnection.cache;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class CompletableFutureManager {

    private final Map<String, CompletableFuture<Void>> waitingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<Void> getOrCreateFuture(String key) {
        return waitingRequests.computeIfAbsent(key, k -> new CompletableFuture<>());
    }

    public void completeFuture(String key) {
        CompletableFuture<Void> future = waitingRequests.remove(key);
        if (future != null) {
            future.complete(null);
        }
    }
}
