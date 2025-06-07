package com.example.solidconnection.community.post.fixture;

import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostImage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class PostImageFixture {

    private final PostImageFixtureBuilder postImageFixtureBuilder;

    public PostImage 게시글_이미지(String url, Post post) {
        return postImageFixtureBuilder
                .url(url)
                .post(post)
                .create();
    }
}
