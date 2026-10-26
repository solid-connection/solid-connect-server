package com.example.solidconnection.community.post.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "view_count_flush", uniqueConstraints = @UniqueConstraint(name = "uk_view_count_flush_id", columnNames = "flush_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ViewCountFlush {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "flush_id", nullable = false, length = 100)
    private String flushId;

    // Receipts survive physical post deletion and late redelivery.
    @Column(name = "post_id", nullable = false)
    private long postId;

    @Column(name = "delta", nullable = false)
    private long delta;
}
