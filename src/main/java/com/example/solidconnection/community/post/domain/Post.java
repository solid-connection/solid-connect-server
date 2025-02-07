package com.example.solidconnection.community.post.domain;

import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.community.comment.domain.Comment;
import com.example.solidconnection.entity.common.BaseEntity;
import com.example.solidconnection.community.post.dto.PostUpdateRequest;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.PostCategory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String title;

    @Column(length = 1000)
    private String content;

    private Boolean isQuestion;

    private Long likeCount;

    private Long viewCount;

    @Enumerated(EnumType.STRING)
    private PostCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_code")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_user_id")
    private SiteUser siteUser;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();

    @BatchSize(size = 5)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> postImageList = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikeList = new ArrayList<>();

    public Post(String title, String content, Boolean isQuestion, Long likeCount, Long viewCount, PostCategory category) {
        this.title = title;
        this.content = content;
        this.isQuestion = isQuestion;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
        this.category = category;
    }

    public void setBoardAndSiteUser(Board board, SiteUser siteUser) {
        if (this.board != null) {
            this.board.getPostList().remove(this);
        }
        this.board = board;
        board.getPostList().add(this);

        if (this.siteUser != null) {
            this.siteUser.getPostList().remove(this);
        }
        this.siteUser = siteUser;
        siteUser.getPostList().add(this);
    }

    public void resetBoardAndSiteUser() {
        if (this.board != null) {
            this.board.getPostList().remove(this);
            this.board = null;
        }
        if (this.siteUser != null) {
            this.siteUser.getPostList().remove(this);
            this.siteUser = null;
        }
    }

    public void update(PostUpdateRequest postUpdateRequest) {
        this.title = postUpdateRequest.title();
        this.content = postUpdateRequest.content();
        this.category = PostCategory.valueOf(postUpdateRequest.postCategory());
    }
}
