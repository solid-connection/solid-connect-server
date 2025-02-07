package com.example.solidconnection.community.post.service;

import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.community.board.repository.BoardRepository;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.community.post.domain.PostImage;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.dto.PostCreateRequest;
import com.example.solidconnection.community.post.dto.PostCreateResponse;
import com.example.solidconnection.community.post.dto.PostDeleteResponse;
import com.example.solidconnection.community.post.dto.PostUpdateRequest;
import com.example.solidconnection.community.post.dto.PostUpdateResponse;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.s3.S3Service;
import com.example.solidconnection.s3.UploadedFileUrlResponse;
import com.example.solidconnection.service.RedisService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.type.BoardCode;
import com.example.solidconnection.type.ImgType;
import com.example.solidconnection.type.PostCategory;
import com.example.solidconnection.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.CAN_NOT_DELETE_OR_UPDATE_QUESTION;
import static com.example.solidconnection.custom.exception.ErrorCode.CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_BOARD_CODE;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_ACCESS;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_CATEGORY;

@Service
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final S3Service s3Service;
    private final RedisService redisService;
    private final RedisUtils redisUtils;

    @Transactional
    public PostCreateResponse createPost(SiteUser siteUser, String code, PostCreateRequest postCreateRequest,
                                         List<MultipartFile> imageFile) {
        // 유효성 검증
        String boardCode = validateCode(code);
        validatePostCategory(postCreateRequest.postCategory());
        validateFileSize(imageFile);

        // 객체 생성
        Board board = boardRepository.getByCode(boardCode);
        Post post = postCreateRequest.toEntity(siteUser, board);
        // 이미지 처리
        savePostImages(imageFile, post);
        Post createdPost = postRepository.save(post);

        return PostCreateResponse.from(createdPost);
    }

    @Transactional
    public PostUpdateResponse updatePost(SiteUser siteUser, String code, Long postId, PostUpdateRequest postUpdateRequest,
                                         List<MultipartFile> imageFile) {
        // 유효성 검증
        String boardCode = validateCode(code);
        Post post = postRepository.getById(postId);
        validateOwnership(post, siteUser);
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

    @Transactional
    public PostDeleteResponse deletePostById(SiteUser siteUser, String code, Long postId) {
        String boardCode = validateCode(code);
        Post post = postRepository.getById(postId);
        validateOwnership(post, siteUser);
        validateQuestion(post);

        removePostImages(post);
        post.resetBoardAndSiteUser();
        // cache out
        redisService.deleteKey(redisUtils.getPostViewCountRedisKey(postId));
        postRepository.deleteById(post.getId());

        return new PostDeleteResponse(postId);
    }

    private String validateCode(String code) {
        try {
            return String.valueOf(BoardCode.valueOf(code));
        } catch (IllegalArgumentException ex) {
            throw new CustomException(INVALID_BOARD_CODE);
        }
    }

    private void validateOwnership(Post post, SiteUser siteUser) {
        if (!post.getSiteUser().getId().equals(siteUser.getId())) {
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

    private void validatePostCategory(String category) {
        if (!EnumUtils.isValidEnum(PostCategory.class, category) || category.equals(PostCategory.전체.toString())) {
            throw new CustomException(INVALID_POST_CATEGORY);
        }
    }

    private void removePostImages(Post post) {
        for (PostImage postImage : post.getPostImageList()) {
            s3Service.deletePostImage(postImage.getUrl());
        }
        post.getPostImageList().clear();
    }
}
