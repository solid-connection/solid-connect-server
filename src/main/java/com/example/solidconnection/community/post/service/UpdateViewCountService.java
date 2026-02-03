package com.example.solidconnection.community.post.service;

import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@EnableAsync
@Slf4j
public class UpdateViewCountService {

    private final PostRepository postRepository;
    private final PostRedisManager postRedisManager;

    @Transactional
    @Async
    public void updateViewCount(String key) {
        Long postId = postRedisManager.getPostIdFromPostViewCountRedisKey(key);
        Post post = postRepository.getById(postId);
        Long viewCount = postRedisManager.getAndDeleteViewCount(key);
        postRepository.increaseViewCount(postId, viewCount);
    }
}
