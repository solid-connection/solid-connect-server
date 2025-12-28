package com.example.solidconnection.siteuser.domain;

import java.time.ZonedDateTime;

import com.example.solidconnection.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@AllArgsConstructor
public class UserBan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "banned_user_id", nullable = false)
    private Long bannedUserId;

    @Column(name = "expired_at", nullable = false)
    private ZonedDateTime expiredAt;

    @Column(name = "is_unbanned", nullable = false)
    private boolean isUnbanned = false;

    @Column(name = "unbanned_by")
    private Long unbannedBy;

    @Column(name = "unbanned_at")
    private ZonedDateTime unbannedAt;

    public UserBan(Long bannedUserId, ZonedDateTime expiredAt) {
        this.bannedUserId = bannedUserId;
        this.expiredAt = expiredAt;
    }

    public void manuallyUnban(Long adminId) {
        this.isUnbanned = true;
        this.unbannedBy = adminId;
        this.unbannedAt = ZonedDateTime.now();
    }
}
