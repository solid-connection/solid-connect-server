package com.example.solidconnection.community.post.service;

import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.community.comment.domain.Comment;
import com.example.solidconnection.community.comment.dto.PostFindCommentResponse;
import com.example.solidconnection.community.post.dto.PostListResponse;
import com.example.solidconnection.community.comment.repository.CommentRepository;
import com.example.solidconnection.community.post.domain.PostImage;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.dto.PostFindPostImageResponse;
import com.example.solidconnection.community.post.dto.PostFindResponse;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.community.post.repository.PostImageRepository;
import com.example.solidconnection.service.RedisService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.support.integration.BaseIntegrationTest;
import com.example.solidconnection.type.BoardCode;
import com.example.solidconnection.type.PostCategory;
import com.example.solidconnection.util.RedisUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("게시글 조회 서비스 테스트")
class PostQueryServiceTest extends BaseIntegrationTest {

    @Autowired
    private PostQueryService postQueryService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Test
    void 게시판_코드와_카테고리로_게시글_목록을_조회한다() {
        // given
        List<Post> posts = List.of(
                미주권_자유게시글, 아시아권_자유게시글, 유럽권_자유게시글, 자유게시판_자유게시글,
                미주권_질문게시글, 아시아권_질문게시글, 유럽권_질문게시글, 자유게시판_질문게시글
        );
        List<Post> expectedPosts = posts.stream()
                .filter(post -> post.getCategory().equals(PostCategory.자유) && post.getBoard().getCode().equals(BoardCode.FREE.name()))
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
        List<Post> posts = List.of(
                미주권_자유게시글, 아시아권_자유게시글, 유럽권_자유게시글, 자유게시판_자유게시글,
                미주권_질문게시글, 아시아권_질문게시글, 유럽권_질문게시글, 자유게시판_질문게시글
        );
        List<Post> expectedPosts = posts.stream()
                .filter(post -> post.getBoard().getCode().equals(BoardCode.FREE.name()))
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
        Post testPost = createPost(자유게시판, 테스트유저_1, expectedImageUrl);
        List<Comment> comments = createComments(testPost, 테스트유저_1, List.of("첫번째 댓글", "두번째 댓글"));

        String validateKey = redisUtils.getValidatePostViewCountRedisKey(테스트유저_1.getId(), testPost.getId());
        String viewCountKey = redisUtils.getPostViewCountRedisKey(testPost.getId());

        // when
        PostFindResponse response = postQueryService.findPostById(
                테스트유저_1,
                자유게시판.getCode(),
                testPost.getId()
        );

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(testPost.getId()),
                () -> assertThat(response.title()).isEqualTo(testPost.getTitle()),
                () -> assertThat(response.content()).isEqualTo(testPost.getContent()),
                () -> assertThat(response.isQuestion()).isEqualTo(testPost.getIsQuestion()),
                () -> assertThat(response.likeCount()).isEqualTo(testPost.getLikeCount()),
                () -> assertThat(response.viewCount()).isEqualTo(testPost.getViewCount()),
                () -> assertThat(response.postCategory()).isEqualTo(String.valueOf(testPost.getCategory())),

                () -> assertThat(response.postFindBoardResponse().code()).isEqualTo(자유게시판.getCode()),
                () -> assertThat(response.postFindBoardResponse().koreanName()).isEqualTo(자유게시판.getKoreanName()),

                () -> assertThat(response.postFindSiteUserResponse().id()).isEqualTo(테스트유저_1.getId()),
                () -> assertThat(response.postFindSiteUserResponse().nickname()).isEqualTo(테스트유저_1.getNickname()),
                () -> assertThat(response.postFindSiteUserResponse().profileImageUrl()).isEqualTo(테스트유저_1.getProfileImageUrl()),

                () -> assertThat(response.postFindPostImageResponses())
                        .hasSize(imageUrls.size())
                        .extracting(PostFindPostImageResponse::url)
                        .containsExactlyElementsOf(imageUrls),

                () -> assertThat(response.postFindCommentResponses())
                        .hasSize(comments.size())
                        .extracting(PostFindCommentResponse::content)
                        .containsExactlyElementsOf(comments.stream().map(Comment::getContent).toList()),

                () -> assertThat(response.isOwner()).isTrue(),
                () -> assertThat(response.isLiked()).isFalse(),

                () -> assertThat(redisService.isKeyExists(viewCountKey)).isTrue(),
                () -> assertThat(redisService.isKeyExists(validateKey)).isTrue()
        );
    }

    private Post createPost(Board board, SiteUser siteUser, String originImageUrl) {
        Post post = new Post(
                "원본 제목",
                "원본 내용",
                false,
                0L,
                0L,
                PostCategory.자유
        );
        post.setBoardAndSiteUser(board, siteUser);
        Post savedPost = postRepository.save(post);
        PostImage postImage = new PostImage(originImageUrl);
        postImage.setPost(savedPost);
        postImageRepository.save(postImage);
        return savedPost;
    }

    private List<Comment> createComments(Post post, SiteUser siteUser, List<String> contents) {
        return contents.stream()
                .map(content -> {
                    Comment comment = new Comment(content);
                    comment.setPostAndSiteUser(post, siteUser);
                    return commentRepository.save(comment);
                })
                .toList();
    }
}
