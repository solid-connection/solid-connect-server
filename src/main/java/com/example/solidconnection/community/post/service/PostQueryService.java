package com.example.solidconnection.community.post.service;

import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.community.board.dto.PostFindBoardResponse;
import com.example.solidconnection.community.comment.dto.PostFindCommentResponse;
import com.example.solidconnection.community.post.dto.PostListResponse;
import com.example.solidconnection.community.board.repository.BoardRepository;
import com.example.solidconnection.community.comment.service.CommentService;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.dto.PostFindPostImageResponse;
import com.example.solidconnection.community.post.dto.PostFindResponse;
import com.example.solidconnection.community.post.repository.PostLikeRepository;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.service.RedisService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.PostFindSiteUserResponse;
import com.example.solidconnection.type.BoardCode;
import com.example.solidconnection.type.PostCategory;
import com.example.solidconnection.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_BOARD_CODE;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_CATEGORY;

@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentService commentService;
    private final RedisService redisService;
    private final RedisUtils redisUtils;

    @Transactional(readOnly = true)
    public List<PostListResponse> findPostsByCodeAndPostCategory(String code, String category) {

        String boardCode = validateCode(code);
        PostCategory postCategory = validatePostCategory(category);

        Board board = boardRepository.getByCodeUsingEntityGraph(boardCode);
        List<Post> postList = getPostListByPostCategory(board.getPostList(), postCategory);

        return PostListResponse.from(postList);
    }

    @Transactional(readOnly = true)
    public PostFindResponse findPostById(SiteUser siteUser, String code, Long postId) {
        String boardCode = validateCode(code);

        Post post = postRepository.getByIdUsingEntityGraph(postId);
        Boolean isOwner = getIsOwner(post, siteUser);
        Boolean isLiked = getIsLiked(post, siteUser);

        PostFindBoardResponse boardPostFindResultDTO = PostFindBoardResponse.from(post.getBoard());
        PostFindSiteUserResponse siteUserPostFindResultDTO = PostFindSiteUserResponse.from(post.getSiteUser());
        List<PostFindPostImageResponse> postImageFindResultDTOList = PostFindPostImageResponse.from(post.getPostImageList());
        List<PostFindCommentResponse> commentFindResultDTOList = commentService.findCommentsByPostId(siteUser, postId);

        // caching && 어뷰징 방지
        if (redisService.isPresent(redisUtils.getValidatePostViewCountRedisKey(siteUser.getId(), postId))) {
            redisService.increaseViewCount(redisUtils.getPostViewCountRedisKey(postId));
        }

        return PostFindResponse.from(
                post, isOwner, isLiked, boardPostFindResultDTO, siteUserPostFindResultDTO, commentFindResultDTOList, postImageFindResultDTOList);
    }

    private String validateCode(String code) {
        try {
            return String.valueOf(BoardCode.valueOf(code));
        } catch (IllegalArgumentException ex) {
            throw new CustomException(INVALID_BOARD_CODE);
        }
    }

    private Boolean getIsOwner(Post post, SiteUser siteUser) {
        return post.getSiteUser().getId().equals(siteUser.getId());
    }

    private Boolean getIsLiked(Post post, SiteUser siteUser) {
        return postLikeRepository.findPostLikeByPostAndSiteUser(post, siteUser)
                .isPresent();
    }

    private PostCategory validatePostCategory(String category) {
        if (!EnumUtils.isValidEnum(PostCategory.class, category)) {
            throw new CustomException(INVALID_POST_CATEGORY);
        }
        return PostCategory.valueOf(category);
    }

    private List<Post> getPostListByPostCategory(List<Post> postList, PostCategory postCategory) {
        if (postCategory.equals(PostCategory.전체)) {
            return postList;
        }
        return postList.stream()
                .filter(post -> post.getCategory().equals(postCategory))
                .collect(Collectors.toList());
    }
}
