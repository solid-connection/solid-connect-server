package com.example.solidconnection.siteuser.domain;

import com.example.solidconnection.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_user_block_blocker_id_blocked_id",
                columnNames = {"blocker_id", "blocked_id"}
        )
})
public class UserBlock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blocker_id", nullable = false)
    private long blockerId;

    @Column(name = "blocked_id", nullable = false)
    private long blockedId;

    public UserBlock(long blockerId, long blockedId) {
        this.blockerId = blockerId;
        this.blockedId = blockedId;
    }
}
