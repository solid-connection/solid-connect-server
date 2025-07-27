package com.example.solidconnection.chat.config;

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

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        List<String> strings = corsProperties.allowedOrigins();
        String[] allowedOrigins = strings.toArray(String[]::new);
        registry.addEndpoint("/connect")
                .setAllowedOrigins(allowedOrigins)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler)
                .taskExecutor()
                .corePoolSize(stompProperties.threadPool().inbound().corePoolSize())
                .maxPoolSize(stompProperties.threadPool().inbound().maxPoolSize())
                .queueCapacity(stompProperties.threadPool().inbound().queueCapacity());
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                .corePoolSize(stompProperties.threadPool().outbound().corePoolSize())
                .maxPoolSize(stompProperties.threadPool().outbound().maxPoolSize());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("wss-heartbeat-");
        scheduler.initialize();

        registry.setApplicationDestinationPrefixes("/publish");
        registry.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{
                        stompProperties.heartbeat().serverInterval(),
                        stompProperties.heartbeat().clientInterval()
                })
                .setTaskScheduler(scheduler);
    }
}
