package com.example.solidconnection.post.service;

import com.example.solidconnection.board.dto.PostFindBoardResponse;
import com.example.solidconnection.board.repository.BoardRepository;
import com.example.solidconnection.comment.dto.PostFindCommentResponse;
import com.example.solidconnection.comment.service.CommentService;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.dto.*;
import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.entity.PostImage;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.dto.*;
import com.example.solidconnection.post.repository.PostRepository;
import com.example.solidconnection.s3.S3Service;
import com.example.solidconnection.s3.UploadedFileUrlResponse;
import com.example.solidconnection.service.RedisService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.dto.PostFindSiteUserResponse;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.type.BoardCode;
import com.example.solidconnection.type.ImgType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final SiteUserRepository siteUserRepository;
    private final BoardRepository boardRepository;
    private final S3Service s3Service;
    private final CommentService commentService;
    private final RedisService redisService;

    private String validateCode(String code) {
        try {
            return String.valueOf(BoardCode.valueOf(code));
        } catch (IllegalArgumentException ex) {
            throw new CustomException(INVALID_BOARD_CODE);
        }
    }

    private void validateOwnership(Post post, String email) {
        if (!post.getSiteUser().getEmail().equals(email)) {
            throw new CustomException(INVALID_POST_ACCESS);
        }
    }

    private void validateFileSize(List<MultipartFile> imageFile) {
        if (imageFile.isEmpty()) {
            return;
        }
        if (imageFile.size() > 5) {
            throw new CustomException(CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES);
        }
    }

    private void validateQuestion(Post post) {
        if (post.getIsQuestion()) {
            throw new CustomException(CAN_NOT_DELETE_OR_UPDATE_QUESTION);
        }
    }

    private Boolean getIsOwner(Post post, String email) {
        return post.getSiteUser().getEmail().equals(email);
    }

    @Transactional
    public PostCreateResponse createPost(String email, String code, PostCreateRequest postCreateRequest,
                                         List<MultipartFile> imageFile) {

        // 유효성 검증
        String boardCode = validateCode(code);
        validateFileSize(imageFile);

        // 객체 생성
        SiteUser siteUser = siteUserRepository.getByEmail(email);
        Board board = boardRepository.getByCode(boardCode);
        Post post = postCreateRequest.toEntity(siteUser, board);
        // 이미지 처리
        savePostImages(imageFile, post);
        Post createdPost = postRepository.save(post);

        return PostCreateResponse.from(createdPost);
    }

    @Transactional
    public PostUpdateResponse updatePost(String email, String code, Long postId, PostUpdateRequest postUpdateRequest,
                                         List<MultipartFile> imageFile) {

        // 유효성 검증
        String boardCode = validateCode(code);
        Post post = postRepository.getById(postId);
        validateOwnership(post, email);
        validateQuestion(post);
        validateFileSize(imageFile);

        // 기존 사진 모두 삭제
        removePostImages(post);
        // 새로운 이미지 등록
        savePostImages(imageFile, post);
        // 게시글 내용 수정
        post.update(postUpdateRequest);

        return PostUpdateResponse.from(post);
    }

    private void savePostImages(List<MultipartFile> imageFile, Post post) {
        if (imageFile.isEmpty()) {
            return;
        }
        List<UploadedFileUrlResponse> uploadedFileUrlResponseList = s3Service.uploadFiles(imageFile, ImgType.COMMUNITY);
        for (UploadedFileUrlResponse uploadedFileUrlResponse : uploadedFileUrlResponseList) {
            PostImage postImage = new PostImage(uploadedFileUrlResponse.fileUrl());
            postImage.setPost(post);
        }
    }

    private void removePostImages(Post post) {
        for (PostImage postImage : post.getPostImageList()) {
            s3Service.deletePostImage(postImage.getUrl());
        }
        post.getPostImageList().clear();
    }

    @Transactional(readOnly = true)
    public PostFindResponse findPostById(String email, String code, Long postId) {

        String boardCode = validateCode(code);

        Post post = postRepository.getByIdUsingEntityGraph(postId);
        Boolean isOwner = getIsOwner(post, email);

        PostFindBoardResponse boardPostFindResultDTO = PostFindBoardResponse.from(post.getBoard());
        PostFindSiteUserResponse siteUserPostFindResultDTO = PostFindSiteUserResponse.from(post.getSiteUser());
        List<PostFindPostImageResponse> postImageFindResultDTOList = PostFindPostImageResponse.from(post.getPostImageList());
        List<PostFindCommentResponse> commentFindResultDTOList = commentService.findCommentsByPostId(email, postId);

        // caching
        redisService.increaseViewCountSync(postId);

        return PostFindResponse.from(
                post, isOwner, boardPostFindResultDTO, siteUserPostFindResultDTO, commentFindResultDTOList, postImageFindResultDTOList);
    }

    @Transactional
    public PostDeleteResponse deletePostById(String email, String code, Long postId) {

        String boardCode = validateCode(code);
        Post post = postRepository.getById(postId);
        validateOwnership(post, email);
        validateQuestion(post);

        removePostImages(post);
        post.resetBoardAndSiteUser();
        // cache out
        redisService.deletePostViewCountKey(postId);
        postRepository.deleteById(post.getId());

        return new PostDeleteResponse(postId);
    }
}
