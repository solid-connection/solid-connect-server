package com.example.solidconnection.community.post.service;

import com.example.solidconnection.community.post.domain.ViewCountEvent;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.community.post.repository.ViewCountFlushRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UpdateViewCountService {

    private final PostRepository postRepository;
    private final ViewCountFlushRepository flushRepository;

    @Transactional
    public void apply(ViewCountEvent event) {
        if (flushRepository.recordIfNew(event.flushId(), event.postId(), event.delta())) {
            // Deleted posts are a completed no-op; retain the receipt against replay.
            postRepository.increaseViewCount(event.postId(), event.delta());
        }
    }
}
