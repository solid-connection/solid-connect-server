package com.example.solidconnection.news.controller;

import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.news.dto.NewsCreateRequest;
import com.example.solidconnection.news.dto.NewsCommandResponse;
import com.example.solidconnection.news.dto.NewsFindResponse;
import com.example.solidconnection.news.dto.NewsResponse;
import com.example.solidconnection.news.dto.NewsUpdateRequest;
import com.example.solidconnection.news.service.NewsCommandService;
import com.example.solidconnection.news.service.NewsQueryService;
import com.example.solidconnection.security.annotation.RequireAdminAccess;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

    private final NewsQueryService newsQueryService;
    private final NewsCommandService newsCommandService;

    // todo: 추후 검색 조건 및 Slice 적용
    @GetMapping
    public ResponseEntity<NewsResponse> searchNews() {
        NewsResponse newsResponse = newsQueryService.searchNews();
        return ResponseEntity.ok(newsResponse);
    }

    @GetMapping(value = "/{news_id}")
    public ResponseEntity<NewsFindResponse> findNewsById(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("news_id") Long newsId
    ) {
        NewsFindResponse newsFindResponse = newsQueryService.findNewsById(newsId);
        return ResponseEntity.ok(newsFindResponse);
    }

    @RequireAdminAccess
    @PostMapping
    public ResponseEntity<NewsCommandResponse> createNews(
            @AuthorizedUser SiteUser siteUser,
            @Valid @RequestPart("newsCreateRequest") NewsCreateRequest newsCreateRequest,
            @RequestParam(value = "file") MultipartFile imageFile
    ) {
        NewsCommandResponse newsCommandResponse = newsCommandService.createNews(newsCreateRequest, imageFile);
        return ResponseEntity.ok(newsCommandResponse);
    }

    @RequireAdminAccess
    @PatchMapping(value = "/{news_id}")
    public ResponseEntity<NewsCommandResponse> updateNews(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("news_id") Long newsId,
            @Valid @RequestPart(value = "newsUpdateRequest") NewsUpdateRequest newsUpdateRequest,
            @RequestParam(value = "file", required = false) MultipartFile imageFile
    ) {
        NewsCommandResponse newsCommandResponse = newsCommandService.updateNews(newsId, newsUpdateRequest, imageFile);
        return ResponseEntity.ok(newsCommandResponse);
    }

    @RequireAdminAccess
    @DeleteMapping(value = "/{news_id}")
    public ResponseEntity<NewsCommandResponse> deleteNewsById(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("news_id") Long newsId
    ) {
        NewsCommandResponse newsCommandResponse = newsCommandService.deleteNewsById(newsId);
        return ResponseEntity.ok(newsCommandResponse);
    }
}
