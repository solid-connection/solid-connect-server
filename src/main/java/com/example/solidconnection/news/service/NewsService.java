package com.example.solidconnection.news.service;

import com.example.solidconnection.news.domain.News;
import com.example.solidconnection.news.dto.NewsCreateRequest;
import com.example.solidconnection.news.dto.NewsResponse;
import com.example.solidconnection.news.repository.NewsRepository;
import com.example.solidconnection.s3.domain.ImgType;
import com.example.solidconnection.s3.dto.UploadedFileUrlResponse;
import com.example.solidconnection.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final S3Service s3Service;
    private final NewsRepository newsRepository;

    public NewsResponse createNews(NewsCreateRequest newsCreateRequest, MultipartFile imageFile) {
        UploadedFileUrlResponse uploadedFile = s3Service.uploadFile(imageFile, ImgType.NEWS);
        News news = newsCreateRequest.toEntity(uploadedFile.fileUrl());
        News savedNews = newsRepository.save(news);
        return NewsResponse.from(savedNews);
    }
}
