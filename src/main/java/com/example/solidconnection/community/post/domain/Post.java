package com.example.solidconnection.community.post.domain;

import com.example.solidconnection.common.BaseEntity;
import com.example.solidconnection.community.comment.domain.Comment;
import com.example.solidconnection.community.post.dto.PostUpdateRequest;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Where;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Where(clause = "is_deleted = false")
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

    @Column
    private String boardCode;

    @Column
    private long siteUserId;

    @Column(name = "is_deleted", columnDefinition = "boolean default false", nullable = false)
    private boolean isDeleted = false;

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

    public void setBoardAndSiteUserId(String boardCode, long siteUserId) {
        this.boardCode = boardCode;
        this.siteUserId = siteUserId;
    }

    public void update(PostUpdateRequest postUpdateRequest) {
        this.title = postUpdateRequest.title();
        this.content = postUpdateRequest.content();
        this.category = PostCategory.valueOf(postUpdateRequest.postCategory());
    }
}
