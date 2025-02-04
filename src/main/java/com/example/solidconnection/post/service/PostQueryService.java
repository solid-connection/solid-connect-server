package com.example.solidconnection.post.service;

import com.example.solidconnection.board.dto.PostFindBoardResponse;
import com.example.solidconnection.comment.dto.PostFindCommentResponse;
import com.example.solidconnection.comment.service.CommentService;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.dto.PostFindPostImageResponse;
import com.example.solidconnection.post.dto.PostFindResponse;
import com.example.solidconnection.post.repository.PostLikeRepository;
import com.example.solidconnection.post.repository.PostRepository;
import com.example.solidconnection.service.RedisService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.PostFindSiteUserResponse;
import com.example.solidconnection.type.BoardCode;
import com.example.solidconnection.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_BOARD_CODE;

@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;
    private final CommentService commentService;
    private final RedisService redisService;
    private final RedisUtils redisUtils;
    private final PostLikeRepository postLikeRepository;

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
}
