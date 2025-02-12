package com.example.solidconnection.community.comment.domain;

import com.example.solidconnection.entity.common.BaseEntity;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Comment extends BaseEntity {

    // for recursive query
    @Transient
    private int level;

    @Transient
    private String path;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_user_id")
    private SiteUser siteUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    private List<Comment> commentList = new ArrayList<>();

    public Comment(String content) {
        this.content = content;
    }

    public void setParentCommentAndPostAndSiteUser(Comment parentComment, Post post, SiteUser siteUser) {

        if (this.parentComment != null) {
            this.parentComment.getCommentList().remove(this);
        }
        this.parentComment = parentComment;
        parentComment.getCommentList().add(this);

        if (this.post != null) {
            this.post.getCommentList().remove(this);
        }
        this.post = post;
        post.getCommentList().add(this);

        if (this.siteUser != null) {
            this.siteUser.getCommentList().remove(this);
        }
        this.siteUser = siteUser;
        siteUser.getCommentList().add(this);
    }

    public void setPostAndSiteUser(Post post, SiteUser siteUser) {

        if (this.post != null) {
            this.post.getCommentList().remove(this);
        }
        this.post = post;
        post.getCommentList().add(this);

        if (this.siteUser != null) {
            this.siteUser.getCommentList().remove(this);
        }
        this.siteUser = siteUser;
        siteUser.getCommentList().add(this);
    }

    public void resetPostAndSiteUserAndParentComment() {
        if (this.post != null) {
            this.post.getCommentList().remove(this);
            this.post = null;
        }
        if (this.siteUser != null) {
            this.siteUser.getCommentList().remove(this);
            this.siteUser = null;
        }
        if (this.parentComment != null) {
            this.parentComment.getCommentList().remove(this);
            this.parentComment = null;
        }
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void deprecateComment() {
        this.content = null;
    }
}
