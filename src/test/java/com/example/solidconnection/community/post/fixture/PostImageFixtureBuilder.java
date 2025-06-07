package com.example.solidconnection.community.post.fixture;

import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostImage;
import com.example.solidconnection.community.post.repository.PostImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class PostImageFixtureBuilder {

    private final PostImageRepository postImageRepository;

    private String url;
    private Post post;

    public PostImageFixtureBuilder url(String url) {
        this.url = url;
        return this;
    }

    public PostImageFixtureBuilder post(Post post) {
        this.post = post;
        return this;
    }

    public PostImage create() {
        PostImage postImage = new PostImage(url);
        postImage.setPost(post);
        return postImageRepository.save(postImage);
    }
}
