package com.example.solidconnection.news.controller;

import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.news.dto.NewsCreateRequest;
import com.example.solidconnection.news.dto.NewsResponse;
import com.example.solidconnection.news.service.NewsService;
import com.example.solidconnection.security.annotation.RequireAdminAccess;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;

    @RequireAdminAccess
    @PostMapping
    public ResponseEntity<NewsResponse> createNews(
            @AuthorizedUser SiteUser siteUser,
            @Valid @RequestPart("newsCreateRequest") NewsCreateRequest newsCreateRequest,
            @RequestParam(value = "file") MultipartFile imageFile
    ) {
        NewsResponse newsResponse = newsService.createNews(newsCreateRequest, imageFile);
        return ResponseEntity.ok(newsResponse);
    }

    @RequireAdminAccess
    @PatchMapping(value = "/{news_id}")
    public ResponseEntity<NewsResponse> updateNews(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("news_id") Long newsId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "url", required = false) String url,
            @RequestParam(value = "file", required = false) MultipartFile imageFile
    ) {
        NewsResponse newsResponse = newsService.updateNews(
                newsId,
                title,
                description,
                url,
                imageFile
        );
        return ResponseEntity.ok(newsResponse);
    }

    @RequireAdminAccess
    @DeleteMapping(value = "/{news_id}")
    public ResponseEntity<NewsResponse> deleteNewsById(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("news_id") Long newsId
    ) {
        NewsResponse newsResponse = newsService.deleteNewsById(newsId);
        return ResponseEntity.ok(newsResponse);
    }
}
