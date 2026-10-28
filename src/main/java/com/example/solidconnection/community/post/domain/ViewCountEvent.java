package com.example.solidconnection.community.post.domain;

public record ViewCountEvent(
        String streamId,
        String flushId,
        long postId,
        long delta
) {

    public ViewCountEvent {
        if (flushId == null || flushId.isBlank() || flushId.length() > 100 || postId <= 0 || delta <= 0) {
            throw new IllegalArgumentException("Invalid view count event");
        }
    }
}
