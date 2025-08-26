package com.example.solidconnection.chat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "websocket")
public record StompProperties(ThreadPool threadPool, HeartbeatProperties heartbeat) {

    public record ThreadPool(InboundProperties inbound, OutboundProperties outbound) {

    }

    public record InboundProperties(int corePoolSize, int maxPoolSize, int queueCapacity) {

    }

    public record OutboundProperties(int corePoolSize, int maxPoolSize) {

    }

    public record HeartbeatProperties(long serverInterval, long clientInterval) {

    }
}