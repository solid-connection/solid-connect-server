package com.example.solidconnection.scheduler;

import static com.example.solidconnection.community.post.service.RedisConstants.VIEW_COUNT_KEY_PATTERN;

import com.example.solidconnection.community.post.service.UpdateViewCountService;
import com.example.solidconnection.util.RedisUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@EnableScheduling
@EnableAsync
public class UpdateViewCountScheduler {

    private final RedisUtils redisUtils;
    private final ThreadPoolTaskExecutor asyncExecutor;
    private final UpdateViewCountService updateViewCountService;

    @Async
    @Scheduled(fixedDelayString = "${view.count.scheduling.delay}")
    public void updateViewCount() {

        List<String> itemViewCountKeys = redisUtils.getKeysOrderByExpiration(VIEW_COUNT_KEY_PATTERN.getValue());

        itemViewCountKeys.forEach(key -> asyncExecutor.submit(() -> {
            updateViewCountService.updateViewCount(key);
        }));
    }
}
