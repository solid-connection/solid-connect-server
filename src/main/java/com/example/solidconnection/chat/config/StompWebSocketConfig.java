package com.example.solidconnection.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    @Value("${websocket.thread-pool.inbound.core-pool-size:6}")
    private int inboundCorePoolSize;

    @Value("${websocket.thread-pool.inbound.max-pool-size:12}")
    private int inboundMaxPoolSize;

    @Value("${websocket.thread-pool.inbound.queue-capacity:1000}")
    private int inboundQueueCapacity;

    @Value("${websocket.thread-pool.outbound.core-pool-size:6}")
    private int outboundCorePoolSize;

    @Value("${websocket.thread-pool.outbound.max-pool-size:12}")
    private int outboundMaxPoolSize;

    @Value("${websocket.heartbeat.server-interval:15000}")
    private long heartbeatServerInterval;

    @Value("${websocket.heartbeat.client-interval:15000}")
    private long heartbeatClientInterval;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connect")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("wss-heartbeat-");
        scheduler.initialize();

        registry.setApplicationDestinationPrefixes("/publish");
        registry.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{heartbeatServerInterval, heartbeatClientInterval})
                .setTaskScheduler(scheduler);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler)
                .taskExecutor()
                .corePoolSize(inboundCorePoolSize)
                .maxPoolSize(inboundMaxPoolSize)
                .queueCapacity(inboundQueueCapacity);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                .corePoolSize(outboundCorePoolSize)
                .maxPoolSize(outboundMaxPoolSize);
    }
}
