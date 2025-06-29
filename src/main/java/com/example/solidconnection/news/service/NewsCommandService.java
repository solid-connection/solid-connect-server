package com.example.solidconnection.news.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.NewsCommandResponse;
import com.example.solidconnection.news.dto.NewsCreateRequest;
import com.example.solidconnection.news.dto.NewsUpdateRequest;
import com.example.solidconnection.news.repository.NewsRepository;
import com.example.solidconnection.s3.domain.ImgType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.example.solidconnection.common.exception.ErrorCode.INVALID_NEWS_ACCESS;
import static com.example.solidconnection.common.exception.ErrorCode.NEWS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class NewsCommandService {

    // todo: default 이미지 URL을 설정하는 로직 필요
    private static final String DEFAULT_IMAGE_URL = "news/default-logo.png";

    private final S3Service s3Service;
    private final NewsRepository newsRepository;

    @Transactional
    public NewsCommandResponse createNews(Long siteUserId,NewsCreateRequest newsCreateRequest, MultipartFile imageFile) {
        String thumbnailUrl = getImageUrl(imageFile);
        News news = newsCreateRequest.toEntity(thumbnailUrl, siteUserId);
        News savedNews = newsRepository.save(news);
        return NewsCommandResponse.from(savedNews);
    }

    @Transactional
    public NewsCommandResponse updateNews(
            Long siteUserId,
            Long newsId,
            NewsUpdateRequest newsUpdateRequest,
            MultipartFile imageFile) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(NEWS_NOT_FOUND));
        validateOwnership(news, siteUserId);
        updateNews(news, newsUpdateRequest);
        updateThumbnail(news, imageFile, newsUpdateRequest.resetToDefaultImage());
        News savedNews = newsRepository.save(news);
        return NewsCommandResponse.from(savedNews);
    }

    private String getImageUrl(MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(imageFile, ImgType.NEWS);
            return uploadedFile.fileUrl();
        }
        return DEFAULT_IMAGE_URL;
    }

    private void validateOwnership(News news, Long siteUserId) {
        if (news.getSiteUserId() != siteUserId) {
            throw new CustomException(INVALID_NEWS_ACCESS);
        }
    }

    private void updateNews(News news, NewsUpdateRequest request) {
        if (hasValue(request.title())) {
            news.updateTitle(request.title());
        }
        if (hasValue(request.description())) {
            news.updateDescription(request.description());
        }
        if (hasValue(request.url())) {
            news.updateUrl(request.url());
        }
    }

    private void updateThumbnail(News news, MultipartFile imageFile, Boolean resetToDefaultImage) {
        if (Boolean.TRUE.equals(resetToDefaultImage)) {
            deleteCustomImage(news.getThumbnailUrl());
            news.updateThumbnailUrl(DEFAULT_IMAGE_URL);
        }
        else if (imageFile != null && !imageFile.isEmpty()) {
            UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(imageFile, ImgType.NEWS);
            deleteCustomImage(news.getThumbnailUrl());
            news.updateThumbnailUrl(uploadedFile.fileUrl());
        }
    }

    private void deleteCustomImage(String imageUrl) {
        if (!DEFAULT_IMAGE_URL.equals(imageUrl)) {
            s3Service.deletePostImage(imageUrl);
        }
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
