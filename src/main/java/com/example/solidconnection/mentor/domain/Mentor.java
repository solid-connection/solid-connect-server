package com.example.solidconnection.mentor.domain;

import com.example.solidconnection.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mentor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "mentee_count")
    private int menteeCount = 0;

    @Column(name = "has_badge")
    private boolean hasBadge = false;

    @Column(name = "introduction", length = 1000)
    private String introduction;

    @Column(name = "pass_tip", length = 1000)
    private String passTip;

    @Column(name = "site_user_id")
    private long siteUserId;

    @Column(name = "university_id")
    private long universityId;

    @Column(nullable = false, name = "term_id")
    private long termId;

    @BatchSize(size = 10)
    @OrderBy("sequence ASC")
    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Channel> channels = new ArrayList<>();

    public Mentor(
            String introduction,
            String passTip,
            long siteUserId,
            Long universityId,
            long termId
    ) {
        this.introduction = introduction;
        this.passTip = passTip;
        this.siteUserId = siteUserId;
        this.universityId = universityId;
        this.termId = termId;
    }

    public Mentor(
            long siteUserId,
            Long universityId,
            long termId
    ) {
        this.siteUserId = siteUserId;
        this.universityId = universityId;
        this.termId = termId;
    }

    public void increaseMenteeCount() {
        this.menteeCount++;
    }

    public void updateIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public void updatePassTip(String passTip) {
        this.passTip = passTip;
    }

    public void updateChannels(List<Channel> channels) {
        int newChannelSize = Math.max(channels.size(), this.channels.size());
        int originalChannelSize = this.channels.size();
        for (int i = 0; i < newChannelSize; i++) {
            if (i < channels.size() && i < this.channels.size()) { // 기존 채널 수정
                Channel existing = this.channels.get(i);
                Channel newChannel = channels.get(i);
                existing.update(newChannel);
            } else if (i < channels.size()) { // 채널 갯수 늘어남 - 새로운 채널 추가
                Channel newChannel = channels.get(i);
                newChannel.updateMentor(this);
                this.channels.add(newChannel);
            } else if (i < originalChannelSize) { // 채널 갯수 줄어듦 - 기존 채널 삭제
                this.channels.remove(this.channels.size() - 1);
            }
        }
    }
}
