package com.example.solidconnection.community.post.service;

import com.example.solidconnection.community.post.domain.ViewCountEvent;
import com.example.solidconnection.community.post.repository.ViewCountStreamRepository;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ViewCountConsumer {

    private final ViewCountStreamRepository streamRepository;
    private final UpdateViewCountService updateService;
    private final Duration minIdle;
    private final String consumer = UUID.randomUUID().toString();

    public ViewCountConsumer(
            ViewCountStreamRepository streamRepository,
            UpdateViewCountService updateService,
            @Value("${view.count.claim-min-idle:60s}") Duration minIdle
    ) {
        this.streamRepository = streamRepository;
        this.updateService = updateService;
        this.minIdle = minIdle;
    }

    public void consume() {
        streamRepository.ensureGroup();
        streamRepository.claim(consumer, minIdle).forEach(this::process);
        streamRepository.readNew(consumer).forEach(this::process);
    }

    public void flushActiveCounters() {
        streamRepository.flush();
    }

    public void process(ViewCountEvent event) {
        try {
            updateService.apply(event);
            // The transactional proxy has committed before apply returns.
            streamRepository.acknowledge(event);
        } catch (RuntimeException exception) {
            log.warn("View count event remains pending: flushId={}", event.flushId(), exception);
        }
    }
}
