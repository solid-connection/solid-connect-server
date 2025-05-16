package com.example.solidconnection.scheduler;

import com.example.solidconnection.service.UpdateViewCountService;
import com.example.solidconnection.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.solidconnection.community.post.service.RedisConstants.VIEW_COUNT_KEY_PATTERN;

@RequiredArgsConstructor
@Component
@EnableScheduling
@EnableAsync
@Slf4j
public class UpdateViewCountScheduler {

    private final RedisUtils redisUtils;
    private final ThreadPoolTaskExecutor asyncExecutor;
    private final UpdateViewCountService updateViewCountService;

    @Async
    @Scheduled(fixedDelayString = "${view.count.scheduling.delay}")
    public void updateViewCount() {

        log.info("updateViewCount thread: {}", Thread.currentThread().getName());
        List<String> itemViewCountKeys = redisUtils.getKeysOrderByExpiration(VIEW_COUNT_KEY_PATTERN.getValue());

        itemViewCountKeys.forEach(key -> asyncExecutor.submit(() -> {
            updateViewCountService.updateViewCount(key);
        }));
    }
}
