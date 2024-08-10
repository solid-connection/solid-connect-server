package com.example.solidconnection.entity;

import com.example.solidconnection.post.domain.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class PostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public PostImage(String url) {
        this.url = url;
    }

    public void setPost(Post post) {
        if (this.post != null) {
            this.post.getPostImageList().remove(this);
        }
        this.post = post;
        post.getPostImageList().add(this);
    }
}
