package com.example.solidconnection.mentor.domain;

import com.example.solidconnection.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    private Long id;

    @Column
    private int menteeCount = 0;

    @Column
    private boolean hasBadge = false;

    @Column(length = 1000)
    private String introduction;

    @Column(length = 1000)
    private String passTip;

    @Column
    private long siteUserId;

    @Column
    private Long universityId; // 임시 멘토일 때, null 가능

    @Column(nullable = false, name = "term_id")
    private long termId;

    @BatchSize(size = 10)
    @OrderBy("sequence ASC")
    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Channel> channels = new ArrayList<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MentorStatus mentorStatus;

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
        this.mentorStatus = MentorStatus.TEMPORARY;
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

    public void createChannels(List<Channel> channels) {
        for(Channel channel : channels) {
            channel.updateMentor(this);
            this.channels.add(channel);
        }
    }
}
