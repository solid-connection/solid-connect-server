package com.example.solidconnection.community.post.service;

import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostLike;
import com.example.solidconnection.community.post.dto.PostDislikeResponse;
import com.example.solidconnection.community.post.dto.PostLikeResponse;
import com.example.solidconnection.community.post.repository.PostLikeRepository;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static com.example.solidconnection.custom.exception.ErrorCode.DUPLICATE_POST_LIKE;
import static com.example.solidconnection.custom.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final SiteUserRepository siteUserRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PostLikeResponse likePost(SiteUser siteUser, Long postId) {
        Post post = postRepository.getById(postId);
        validateDuplicatePostLike(post, siteUser);
        PostLike postLike = new PostLike();

        /*
         * todo: siteUser를 영속 상태로 만들 수 있도록 컨트롤러에서 siteUserId 를 넘겨줄 것인지,
         *  siteUser 에 postList 를 FetchType.EAGER 로 설정할 것인지,
         *  post 와 siteUser 사이의 양방향을 끊을 것인지 생각해봐야한다.
         */
        SiteUser siteUser1 = siteUserRepository.findById(siteUser.getId()).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        postLike.setPostAndSiteUser(post, siteUser1);
        postLikeRepository.save(postLike);
        postRepository.increaseLikeCount(post.getId());

        return PostLikeResponse.from(postRepository.getById(postId)); // 실시간성을 위한 재조회
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PostDislikeResponse dislikePost(SiteUser siteUser, Long postId) {
        Post post = postRepository.getById(postId);

        PostLike postLike = postLikeRepository.getByPostAndSiteUser(post, siteUser);
        postLike.resetPostAndSiteUser();
        postLikeRepository.deleteById(postLike.getId());
        postRepository.decreaseLikeCount(post.getId());

        return PostDislikeResponse.from(postRepository.getById(postId)); // 실시간성을 위한 재조회
    }

    private void validateDuplicatePostLike(Post post, SiteUser siteUser) {
        if (postLikeRepository.findPostLikeByPostAndSiteUser(post, siteUser).isPresent()) {
            throw new CustomException(DUPLICATE_POST_LIKE);
        }
    }
}
