package com.example.solidconnection.siteuser.domain;

import static java.time.ZoneOffset.UTC;

import java.time.ZonedDateTime;
import com.example.solidconnection.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserBan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "banned_user_id", nullable = false)
    private Long bannedUserId;

    @Column(name = "banned_by", nullable = false)
    private Long bannedBy;

    @Column(name = "duration", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserBanDuration duration;

    @Column(name = "expired_at", nullable = false)
    private ZonedDateTime expiredAt;

    @Column(name = "is_expired", nullable = false)
    private boolean isExpired = false;

    @Column(name = "unbanned_by")
    private Long unbannedBy;

    @Column(name = "unbanned_at")
    private ZonedDateTime unbannedAt;

    public UserBan(Long bannedUserId, Long bannedBy, UserBanDuration duration, ZonedDateTime expiredAt) {
        this.bannedUserId = bannedUserId;
        this.bannedBy = bannedBy;
        this.duration = duration;
        this.expiredAt = expiredAt;
    }

    public void manuallyUnban(Long adminId) {
        this.isExpired = true;
        this.unbannedBy = adminId;
        this.unbannedAt = ZonedDateTime.now(UTC);
    }
}
