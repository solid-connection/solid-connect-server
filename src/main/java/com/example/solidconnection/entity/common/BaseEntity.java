package com.example.solidconnection.entity.common;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MICROS;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@DynamicUpdate
@DynamicInsert
public abstract class BaseEntity {

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = ZonedDateTime.now(UTC).truncatedTo(MICROS); // 나노초 6자리 까지만 저장
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = ZonedDateTime.now(UTC).truncatedTo(MICROS);
    }
}
