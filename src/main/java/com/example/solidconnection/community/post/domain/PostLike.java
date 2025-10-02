package com.example.solidconnection.community.post.domain;

import com.example.solidconnection.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class PostLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    private long siteUserId;

    public void setPostAndSiteUserId(Post post, long siteUserId) {
        if (this.post != null) {
            this.post.getPostLikeList().remove(this);
        }
        this.post = post;
        post.getPostLikeList().add(this);
        this.siteUserId = siteUserId;
    }

    public void resetPost() {
        if (this.post != null) {
            this.post.getPostLikeList().remove(this);
        }
        this.post = null;
    }
}
