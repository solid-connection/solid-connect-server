package com.example.solidconnection.news.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.news.config.NewsProperties;
import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.NewsCommandResponse;
import com.example.solidconnection.news.dto.NewsCreateRequest;
import com.example.solidconnection.news.dto.NewsUpdateRequest;
import com.example.solidconnection.news.repository.NewsRepository;
import com.example.solidconnection.s3.domain.ImgType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.example.solidconnection.common.exception.ErrorCode.INVALID_NEWS_ACCESS;
import static com.example.solidconnection.common.exception.ErrorCode.NEWS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class NewsCommandService {

    private final S3Service s3Service;
    private final NewsProperties newsProperties;
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
        news.updateNews(newsUpdateRequest.title(), newsUpdateRequest.description(), newsUpdateRequest.url());
        updateThumbnail(news, imageFile, newsUpdateRequest.resetToDefaultImage());
        News savedNews = newsRepository.save(news);
        return NewsCommandResponse.from(savedNews);
    }

    @Transactional
    public NewsCommandResponse deleteNewsById(SiteUser siteUser, Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(NEWS_NOT_FOUND));
        validatePermission(siteUser, news);
        deleteCustomImage(news.getThumbnailUrl());
        newsRepository.delete(news);
        return NewsCommandResponse.from(news);
    }

    private String getImageUrl(MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(imageFile, ImgType.NEWS);
            return uploadedFile.fileUrl();
        }
        return newsProperties.defaultThumbnailUrl();
    }

    private void validateOwnership(News news, Long siteUserId) {
        if (news.getSiteUserId() != siteUserId) {
            throw new CustomException(INVALID_NEWS_ACCESS);
        }
    }

    private void validatePermission(SiteUser currentUser, News news) {
        boolean isOwner = news.getSiteUserId() == currentUser.getId();
        boolean isAdmin = currentUser.getRole().equals(Role.ADMIN);
        if (!isOwner && !isAdmin) {
            throw new CustomException(INVALID_NEWS_ACCESS);
        }
    }

    private void updateThumbnail(News news, MultipartFile imageFile, Boolean resetToDefaultImage) {
        if (Boolean.TRUE.equals(resetToDefaultImage)) {
            deleteCustomImage(news.getThumbnailUrl());
            news.updateThumbnailUrl(newsProperties.defaultThumbnailUrl());
        }
        else if (imageFile != null && !imageFile.isEmpty()) {
            UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(imageFile, ImgType.NEWS);
            deleteCustomImage(news.getThumbnailUrl());
            news.updateThumbnailUrl(uploadedFile.fileUrl());
        }
    }

    private void deleteCustomImage(String imageUrl) {
        if (!newsProperties.defaultThumbnailUrl().equals(imageUrl)) {
            s3Service.deletePostImage(imageUrl);
        }
    }
}
