package com.example.solidconnection.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class StompSessionTimeoutConfig {

    @Bean(defaultCandidate = false)
    public ThreadPoolTaskScheduler stompSubscriptionTimeoutScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("stomp-subscription-timeout-");
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.setWaitForTasksToCompleteOnShutdown(false);
        return scheduler;
    }

    @Bean(defaultCandidate = false)
    public TaskExecutor stompSessionCloseExecutor() {
        return new VirtualThreadTaskExecutor("stomp-session-close-");
    }
}
