package com.example.solidconnection.mentor.domain;

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
public class Mentor {

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
    private long universityId;

    @Column(length = 50, nullable = false)
    private String term;

    @BatchSize(size = 10)
    @OrderBy("sequence ASC")
    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Channel> channels = new ArrayList<>();

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
        this.channels.clear();
        if (channels == null || channels.isEmpty()) {
            return;
        }
        for (Channel channel : channels) {
            channel.updateMentor(this);
            this.channels.add(channel);
        }
    }
}
