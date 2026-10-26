package com.example.solidconnection.community.post.repository;

import com.example.solidconnection.community.post.domain.ViewCountEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class ViewCountStreamRepository {

    public static final String ACTIVE_KEY = "post:view:{views}:active";
    public static final String STREAM_KEY = "post:view:{views}:events";
    public static final String GROUP = "view-count-db";
    private static final int BATCH_SIZE = 50;
    private static final RedisScript<Long> FLUSH = RedisScript.of(new ClassPathResource("scripts/flushViewCounts.lua"), Long.class);
    private static final RedisScript<List> CLAIM = RedisScript.of(new ClassPathResource("scripts/claimViewCounts.lua"), List.class);
    private static final RedisScript<Long> ACK = RedisScript.of(new ClassPathResource("scripts/ackViewCount.lua"), Long.class);
    private static final RedisScript<Long> INIT_GROUP = RedisScript.of("""
            local result = redis.pcall('XGROUP', 'CREATE', KEYS[1], ARGV[1], '0', 'MKSTREAM')
            if type(result) == 'table' and result.err and not string.find(result.err, 'BUSYGROUP', 1, true) then
                return redis.error_reply(result.err)
            end
            return 1
            """, Long.class);

    private final StringRedisTemplate redis;
    private String claimCursor = "0-0";

    public ViewCountStreamRepository(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void increment(long postId) {
        redis.opsForHash().increment(ACTIVE_KEY, Long.toString(postId), 1);
    }

    public void deleteActive(long postId) {
        redis.opsForHash().delete(ACTIVE_KEY, Long.toString(postId));
    }

    public long flush() {
        Long count = redis.execute(FLUSH, List.of(ACTIVE_KEY, STREAM_KEY), UUID.randomUUID().toString(), Integer.toString(BATCH_SIZE));
        return count == null ? 0 : count;
    }

    public void ensureGroup() {
        redis.execute(INIT_GROUP, List.of(STREAM_KEY), GROUP);
    }

    public List<ViewCountEvent> readNew(String consumer) {
        List<MapRecord<String, Object, Object>> records = redis.opsForStream().read(
                Consumer.from(GROUP, consumer), StreamReadOptions.empty().count(BATCH_SIZE),
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()));
        if (records == null) {
            return List.of();
        }
        return records.stream().flatMap(record -> {
            Map<Object, Object> fields = record.getValue();
            return parseEvent(record.getId().getValue(), Objects.toString(fields.get("flushId"), ""),
                    Objects.toString(fields.get("postId"), ""), Objects.toString(fields.get("delta"), "")).stream();
        }).toList();
    }

    public synchronized List<ViewCountEvent> claim(String consumer, Duration minIdle) {
        List<?> response = redis.execute(CLAIM, List.of(STREAM_KEY), GROUP, consumer,
                Long.toString(minIdle.toMillis()), claimCursor, Integer.toString(BATCH_SIZE));
        if (response == null || response.isEmpty()) {
            return List.of();
        }
        claimCursor = response.get(0).toString();
        List<ViewCountEvent> events = new ArrayList<>();
        for (int i = 1; i < response.size(); i += 4) {
            parseEvent(response.get(i).toString(), response.get(i + 1).toString(),
                    response.get(i + 2).toString(), response.get(i + 3).toString()).ifPresent(events::add);
        }
        return events;
    }

    private Optional<ViewCountEvent> parseEvent(
            String streamId,
            String flushId,
            String postId,
            String delta
    ) {
        try {
            return Optional.of(new ViewCountEvent(streamId, flushId, Long.parseLong(postId), Long.parseLong(delta)));
        } catch (IllegalArgumentException exception) {
            log.warn("Invalid view count event remains pending: streamId={}", streamId, exception);
            return Optional.empty();
        }
    }

    public void acknowledge(ViewCountEvent event) {
        redis.execute(ACK, List.of(STREAM_KEY), GROUP, event.streamId());
    }
}
