package com.example.solidconnection.news.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.NewsCreateRequest;
import com.example.solidconnection.news.dto.NewsCommandResponse;
import com.example.solidconnection.news.repository.NewsRepository;
import com.example.solidconnection.s3.domain.ImgType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.example.solidconnection.common.exception.ErrorCode.NEWS_DESCRIPTION_TOO_LONG;
import static com.example.solidconnection.common.exception.ErrorCode.NEWS_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.NEWS_TITLE_EMPTY;
import static com.example.solidconnection.common.exception.ErrorCode.NEWS_TITLE_TOO_LONG;
import static com.example.solidconnection.common.exception.ErrorCode.NEWS_URL_INVALID;
import static com.example.solidconnection.common.exception.ErrorCode.NEWS_URL_TOO_LONG;

@Service
@RequiredArgsConstructor
public class NewsCommandService {

    public static final int MAX_TITLE_LENGTH = 255;
    public static final int MAX_DESCRIPTION_LENGTH = 255;
    public static final int MAX_URL_LENGTH = 500;

    private final S3Service s3Service;
    private final NewsRepository newsRepository;

    @Transactional
    public NewsCommandResponse createNews(NewsCreateRequest newsCreateRequest, MultipartFile imageFile) {
        UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(imageFile, ImgType.NEWS);
        News news = newsCreateRequest.toEntity(uploadedFile.fileUrl());
        News savedNews = newsRepository.save(news);
        return NewsCommandResponse.from(savedNews);
    }

    @Transactional
    public NewsCommandResponse updateNews(Long newsId, String title, String description, String url, MultipartFile imageFile) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(NEWS_NOT_FOUND));
        if (title != null) {
            validateTitle(title);
            news.updateTitle(title);
        }
        if (description != null) {
            validateDescription(description);
            news.updateDescription(description);
        }
        if (url != null) {
            validateUrl(url);
            news.updateUrl(url);
        }
        if (imageFile != null) {
            s3Service.deletePostImage(news.getThumbnailUrl());
            UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(imageFile, ImgType.NEWS);
            String thumbnailImageUrl = uploadedFile.fileUrl();
            news.updateThumbnailUrl(thumbnailImageUrl);
        }
        News savedNews = newsRepository.save(news);
        return NewsCommandResponse.from(savedNews);
    }

    @Transactional
    public NewsCommandResponse deleteNewsById(Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(NEWS_NOT_FOUND));
        s3Service.deletePostImage(news.getThumbnailUrl());
        newsRepository.deleteById(newsId);
        return NewsCommandResponse.from(news);
    }

    private void validateTitle(String title) {
        if (title.trim().isEmpty()) {
            throw new CustomException(NEWS_TITLE_EMPTY);
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new CustomException(NEWS_TITLE_TOO_LONG);
        }
    }

    private void validateDescription(String description) {
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new CustomException(NEWS_DESCRIPTION_TOO_LONG);
        }
    }

    private void validateUrl(String url) {
        if (!url.matches("^https?://.*")) {
            throw new CustomException(NEWS_URL_INVALID);
        }
        if (url.length() > MAX_URL_LENGTH) {
            throw new CustomException(NEWS_URL_TOO_LONG);
        }
    }
}
