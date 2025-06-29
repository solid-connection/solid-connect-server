package com.example.solidconnection.news.service;

import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.NewsCommandResponse;
import com.example.solidconnection.news.dto.NewsCreateRequest;
import com.example.solidconnection.news.repository.NewsRepository;
import com.example.solidconnection.s3.domain.ImgType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class NewsCommandService {

    private final S3Service s3Service;
    private final NewsRepository newsRepository;

    @Transactional
    public NewsCommandResponse createNews(Long siteUserId,NewsCreateRequest newsCreateRequest, MultipartFile imageFile) {
        String thumbnailUrl = getImageUrl(imageFile);
        News news = newsCreateRequest.toEntity(thumbnailUrl, siteUserId);
        News savedNews = newsRepository.save(news);
        return NewsCommandResponse.from(savedNews);
    }

    private String getImageUrl(MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(imageFile, ImgType.NEWS);
            return uploadedFile.fileUrl();
        }
        // todo: default 이미지 URL을 설정하는 로직 필요
        return "https://default-logo.png";
    }
}
