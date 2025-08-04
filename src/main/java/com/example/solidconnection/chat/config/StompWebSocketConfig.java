package com.example.solidconnection.chat.config;

import com.example.solidconnection.chat.config.StompProperties.HeartbeatProperties;
import com.example.solidconnection.chat.config.StompProperties.InboundProperties;
import com.example.solidconnection.chat.config.StompProperties.OutboundProperties;
import com.example.solidconnection.security.config.CorsProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
    private final StompProperties stompProperties;
    private final CorsProperties corsProperties;
    private final WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;
    private final CustomHandshakeHandler customHandshakeHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        List<String> strings = corsProperties.allowedOrigins();
        String[] allowedOrigins = strings.toArray(String[]::new);
        registry.addEndpoint("/connect")
                .setAllowedOrigins(allowedOrigins)
                .addInterceptors(webSocketHandshakeInterceptor)
                .setHandshakeHandler(customHandshakeHandler)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        InboundProperties inboundProperties = stompProperties.threadPool().inbound();
        registration.interceptors(stompHandler).taskExecutor().corePoolSize(inboundProperties.corePoolSize()).maxPoolSize(inboundProperties.maxPoolSize()).queueCapacity(inboundProperties.queueCapacity());
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        OutboundProperties outboundProperties = stompProperties.threadPool().outbound();
        registration.taskExecutor().corePoolSize(outboundProperties.corePoolSize()).maxPoolSize(outboundProperties.maxPoolSize());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("wss-heartbeat-");
        scheduler.initialize();
        HeartbeatProperties heartbeatProperties = stompProperties.heartbeat();
        registry.setApplicationDestinationPrefixes("/publish");
        registry.enableSimpleBroker("/topic").setHeartbeatValue(new long[]{heartbeatProperties.serverInterval(), heartbeatProperties.clientInterval()}).setTaskScheduler(scheduler);
    }
}
