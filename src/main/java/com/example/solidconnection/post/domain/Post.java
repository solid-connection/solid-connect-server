package com.example.solidconnection.post.domain;

import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.entity.Comment;
import com.example.solidconnection.entity.PostImage;
import com.example.solidconnection.entity.common.BaseEntity;
import com.example.solidconnection.entity.mapping.PostLike;
import com.example.solidconnection.post.dto.PostUpdateRequest;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.PostCategory;
import jakarta.persistence.*;
import lombok.*;
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
        this.title = postUpdateRequest.getTitle();
        this.content = postUpdateRequest.getContent();
        this.category = PostCategory.valueOf(postUpdateRequest.getPostCategory());
    }

    public void increaseViewCount(Long updateViewCount) {
        this.viewCount += updateViewCount;
    }

}
