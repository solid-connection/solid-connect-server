package com.example.solidconnection.unit.repository;

import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.board.repository.BoardRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.PostImage;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.repository.PostRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PostCategory;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("게시글 레포지토리 테스트")
public class PostRepositoryTest {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private SiteUserRepository siteUserRepository;

    private Post post;
    private Board board;
    private SiteUser siteUser;

    @BeforeEach
    public void setUp() {
        board = createBoard();
        boardRepository.save(board);
        siteUser = createSiteUser();
        siteUserRepository.save(siteUser);
        post = createPostWithImages(board, siteUser);
        post = postRepository.save(post);
    }

    private SiteUser createSiteUser() {
        return new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
    }

    private Board createBoard() {
        return new Board(
                "FREE", "자유게시판");
    }

    private Post createPostWithImages(Board board, SiteUser siteUser) {
        Post postWithImages = new Post(
                "title",
                "content",
                false,
                0L,
                0L,
                PostCategory.valueOf("자유")
        );
        postWithImages.setBoardAndSiteUser(board, siteUser);

        List<PostImage> postImageList = new ArrayList<>();
        postImageList.add(new PostImage("https://s3.example.com/test1.png"));
        postImageList.add(new PostImage("https://s3.example.com/test2.png"));
        for (PostImage postImage : postImageList) {
            postImage.setPost(postWithImages);
        }
        return postWithImages;
    }

    @Test
    @Transactional
    public void 게시글을_조회할_때_게시글_이미지는_즉시_로딩한다() {
        Post foundPost = postRepository.getByIdUsingEntityGraph(post.getId());
        foundPost.getPostImageList().size(); // 추가쿼리 발생하지 않는다.

        assertThat(foundPost).isEqualTo(post);
    }

    @Test
    @Transactional
    public void 게시글을_조회할_때_게시글_이미지는_즉시_로딩한다_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // given
        Long invalidId = -1L;

        // when, then
        CustomException exception = assertThrows(CustomException.class, () -> {
            postRepository.getByIdUsingEntityGraph(invalidId);
        });
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_POST_ID.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_POST_ID.getCode());
    }

    @Test
    @Transactional
    public void 게시글을_조회한다() {
        Post foundPost = postRepository.getById(post.getId());

        assertEquals(post, foundPost);
    }

    @Test
    @Transactional
    public void 게시글을_조회할_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        Long invalidId = -1L;

        CustomException exception = assertThrows(CustomException.class, () -> {
            postRepository.getById(invalidId);
        });
        assertThat(exception.getMessage())
                .isEqualTo(INVALID_POST_ID.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(INVALID_POST_ID.getCode());
    }
}
