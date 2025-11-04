package com.example.solidconnection.community.post.fixture;

import com.example.solidconnection.common.helper.TestTimeHelper;
import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostCategory;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class PostFixtureBuilder {

    private final PostRepository postRepository;

    private String title;
    private String content;
    private Boolean isQuestion;
    private Long likeCount;
    private Long viewCount;
    private PostCategory postCategory;
    private Board board;
    private SiteUser siteUser;

    public PostFixtureBuilder title(String title) {
        this.title = title;
        return this;
    }

    public PostFixtureBuilder content(String content) {
        this.content = content;
        return this;
    }

    public PostFixtureBuilder isQuestion(Boolean isQuestion) {
        this.isQuestion = isQuestion;
        return this;
    }

    public PostFixtureBuilder likeCount(Long likeCount) {
        this.likeCount = likeCount;
        return this;
    }

    public PostFixtureBuilder viewCount(Long viewCount) {
        this.viewCount = viewCount;
        return this;
    }

    public PostFixtureBuilder postCategory(PostCategory postCategory) {
        this.postCategory = postCategory;
        return this;
    }

    public PostFixtureBuilder board(Board board) {
        this.board = board;
        return this;
    }

    public PostFixtureBuilder siteUser(SiteUser siteUser) {
        this.siteUser = siteUser;
        return this;
    }

    public Post create() {
        Post post = new Post(
                title,
                content,
                isQuestion,
                likeCount,
                viewCount,
                postCategory);
        post.setBoardAndSiteUserId(board.getCode(), siteUser.getId());
        return postRepository.save(post);
    }

    public Post createWithDelaySeconds(long seconds) {
        Post post = new Post(
                title,
                content,
                isQuestion,
                likeCount,
                viewCount,
                postCategory);
        post.setBoardAndSiteUserId(board.getCode(), siteUser.getId());

        Post saved = postRepository.save(post);

        TestTimeHelper.setCreatedAt(saved, saved.getCreatedAt().plusSeconds(seconds));
        return postRepository.save(post);
    }
}
