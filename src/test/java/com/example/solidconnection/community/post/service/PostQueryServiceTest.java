package com.example.solidconnection.community.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.community.board.domain.BoardCode;
import com.example.solidconnection.community.board.fixture.BoardFixture;
import com.example.solidconnection.community.comment.domain.Comment;
import com.example.solidconnection.community.comment.fixture.CommentFixture;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostCategory;
import com.example.solidconnection.community.post.dto.PostFindResponse;
import com.example.solidconnection.community.post.dto.PostListResponse;
import com.example.solidconnection.community.post.fixture.PostFixture;
import com.example.solidconnection.community.post.fixture.PostImageFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.util.RedisUtils;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("게시글 조회 서비스 테스트")
class PostQueryServiceTest {

    @Autowired
    private PostQueryService postQueryService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private BoardFixture boardFixture;

    @Autowired
    private PostFixture postFixture;

    @Autowired
    private PostImageFixture postImageFixture;

    @Autowired
    private CommentFixture commentFixture;

    private SiteUser user;
    private Post post1;
    private Post post2;
    private Post post3;

    @BeforeEach
    void setUp() {
        user = siteUserFixture.사용자();
        post1 = postFixture.게시글(
                "제목1",
                "내용1",
                false,
                PostCategory.자유,
                boardFixture.자유게시판(),
                user
        );
        post2 = postFixture.게시글(
                "제목2",
                "내용2",
                false,
                PostCategory.자유,
                boardFixture.미주권(),
                user
        );
        post3 = postFixture.게시글(
                "제목3",
                "내용3",
                true,
                PostCategory.질문,
                boardFixture.자유게시판(),
                user
        );
    }

    @Test
    void 게시판_코드와_카테고리로_게시글_목록을_조회한다() {
        // given
        List<Post> posts = List.of(post1, post2, post3);
        List<Post> expectedPosts = posts.stream()
                .filter(post -> post.getCategory().equals(PostCategory.자유)
                        && post.getBoardCode().equals(BoardCode.FREE.name()))
                .toList();
        List<PostListResponse> expectedResponses = PostListResponse.from(expectedPosts);

        // when
        List<PostListResponse> actualResponses = postQueryService.findPostsByCodeAndPostCategory(
                BoardCode.FREE.name(),
                PostCategory.자유.name()
        );

        // then
        assertThat(actualResponses)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ZonedDateTime.class)
                .isEqualTo(expectedResponses);
    }

    @Test
    void 전체_카테고리로_조회시_해당_게시판의_모든_게시글을_조회한다() {
        // given
        List<Post> posts = List.of(post1, post2, post3);
        List<Post> expectedPosts = posts.stream()
                .filter(post -> post.getBoardCode().equals(BoardCode.FREE.name()))
                .toList();
        List<PostListResponse> expectedResponses = PostListResponse.from(expectedPosts);

        // when
        List<PostListResponse> actualResponses = postQueryService.findPostsByCodeAndPostCategory(
                BoardCode.FREE.name(),
                PostCategory.전체.name()
        );

        // then
        assertThat(actualResponses)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ZonedDateTime.class)
                .isEqualTo(expectedResponses);
    }

    @Test
    void 게시글을_성공적으로_조회한다() {
        // given
        String expectedImageUrl = "test-image-url";
        List<String> imageUrls = List.of(expectedImageUrl);
        Post post = postFixture.게시글(
                "제목",
                "내용",
                false,
                PostCategory.자유,
                boardFixture.자유게시판(),
                user
        );
        postImageFixture.게시글_이미지(expectedImageUrl, post);
        Comment comment1 = commentFixture.부모_댓글("댓글1", post, user);
        Comment comment2 = commentFixture.부모_댓글("댓글2", post, user);
        List<Comment> comments = List.of(comment1, comment2);

        String validateKey = redisUtils.getValidatePostViewCountRedisKey(user.getId(), post.getId());
        String viewCountKey = redisUtils.getPostViewCountRedisKey(post.getId());

        // when
        PostFindResponse response = postQueryService.findPostById(user.getId(), post.getId());

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(post.getId()),
                () -> assertThat(response.postFindBoardResponse().code()).isEqualTo(boardFixture.자유게시판().getCode()),
                () -> assertThat(response.postFindSiteUserResponse().id()).isEqualTo(user.getId()),
                () -> assertThat(response.postFindPostImageResponses()).hasSize(imageUrls.size()),
                () -> assertThat(response.postFindCommentResponses()).hasSize(comments.size()),
                () -> assertThat(redisService.isKeyExists(viewCountKey)).isTrue(),
                () -> assertThat(redisService.isKeyExists(validateKey)).isTrue()
        );
    }

    @Test
    void 게시글_목록_조회시_첫번째_이미지를_썸네일로_반환한다() {
        // given
        String firstImageUrl = "first-thumbnail-url";
        String secondImageUrl = "second-thumbnail-url";
        postImageFixture.게시글_이미지(firstImageUrl, post1);
        postImageFixture.게시글_이미지(secondImageUrl, post1);

        // when
        List<PostListResponse> actualResponses = postQueryService.findPostsByCodeAndPostCategory(
                BoardCode.FREE.name(),
                PostCategory.전체.name()
        );

        // then
        PostListResponse postResponse = actualResponses.stream()
                .filter(p -> p.id().equals(post1.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(postResponse.postThumbnailUrl()).isEqualTo(firstImageUrl);
    }

    @Test
    void 게시글에_이미지가_없다면_썸네일로_null을_반환한다() {
        // when
        List<PostListResponse> actualResponses = postQueryService.findPostsByCodeAndPostCategory(
                BoardCode.FREE.name(),
                PostCategory.전체.name()
        );

        // then
        PostListResponse postResponse = actualResponses.stream()
                .filter(p -> p.id().equals(post3.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(postResponse.postThumbnailUrl()).isNull();
    }
}
