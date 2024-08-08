package com.example.solidconnection.scheduler;

import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.repository.PostRepository;
import com.example.solidconnection.service.RedisService;
import com.example.solidconnection.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.solidconnection.type.RedisConstants.*;

@RequiredArgsConstructor
@Component
@EnableScheduling
@Slf4j
public class RedisScheduler {

    private final PostRepository postRepository;
    private final RedisUtils redisUtils;
    private final RedisService redisService;

    @Scheduled(fixedDelayString = "${view.count.scheduling.delay}")
    @Transactional
    public void updateViewCount() {

        List<String> itemViewCountKeys = redisUtils.getKeysOrderByExpiration(VIEW_COUNT_KEY_PATTERN.getValue());

        if (itemViewCountKeys.isEmpty()) {
            return;
        }

        itemViewCountKeys.forEach(key -> {
            Post post = postRepository.getById(extractPostIdFromViewCount(key));
            post.increaseViewCount(redisService.getViewCountValueAndDeleteKey(key));
        });
        log.info("db 정합성 맞추기 끝");
    }

    private Long extractPostIdFromViewCount(String key) {
        return Long.parseLong(key.substring(VIEW_COUNT_KEY_PREFIX.getValue().length()));
    }
}
