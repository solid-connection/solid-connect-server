package com.example.solidconnection.community.post.domain;

import com.example.solidconnection.siteuser.domain.SiteUser;
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
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_user_id")
    private SiteUser siteUser;

    public void setPostAndSiteUser(Post post, SiteUser siteUser) {
        if (this.post != null) {
            this.post.getPostLikeList().remove(this);
        }
        this.post = post;
        post.getPostLikeList().add(this);

        if (this.siteUser != null) {
            this.siteUser.getPostLikeList().remove(this);
        }
        this.siteUser = siteUser;
        siteUser.getPostLikeList().add(this);
    }

    public void resetPostAndSiteUser() {
        if (this.post != null) {
            this.post.getPostLikeList().remove(this);
        }
        this.post = null;

        if (this.siteUser != null) {
            this.siteUser.getPostLikeList().remove(this);
        }
        this.siteUser = null;
    }
}
