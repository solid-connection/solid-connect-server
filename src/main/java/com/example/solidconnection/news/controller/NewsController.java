package com.example.solidconnection.news.controller;

import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.news.dto.NewsCommandResponse;
import com.example.solidconnection.news.dto.NewsCreateRequest;
import com.example.solidconnection.news.dto.NewsUpdateRequest;
import com.example.solidconnection.news.service.NewsCommandService;
import com.example.solidconnection.security.annotation.RequireRoleAccess;
import com.example.solidconnection.siteuser.domain.Role;
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

    private final NewsCommandService newsCommandService;

    @RequireRoleAccess(roles = {Role.ADMIN, Role.MENTOR})
    @PostMapping
    public ResponseEntity<NewsCommandResponse> createNews(
            @AuthorizedUser SiteUser siteUser,
            @Valid @RequestPart("newsCreateRequest") NewsCreateRequest newsCreateRequest,
            @RequestParam(value = "file", required = false) MultipartFile imageFile
    ) {
        NewsCommandResponse newsCommandResponse = newsCommandService.createNews(siteUser.getId(), newsCreateRequest, imageFile);
        return ResponseEntity.ok(newsCommandResponse);
    }

    @RequireRoleAccess(roles = {Role.ADMIN, Role.MENTOR})
    @PatchMapping("/{news-id}")
    public ResponseEntity<NewsCommandResponse> updateNews(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("news-id") Long newsId,
            @Valid @RequestPart(value = "newsUpdateRequest") NewsUpdateRequest newsUpdateRequest,
            @RequestParam(value = "file", required = false) MultipartFile imageFile
    ) {
        NewsCommandResponse newsCommandResponse = newsCommandService.updateNews(
                siteUser.getId(),
                newsId,
                newsUpdateRequest,
                imageFile);
        return ResponseEntity.ok(newsCommandResponse);
    }

    @RequireRoleAccess(roles = {Role.ADMIN, Role.MENTOR})
    @DeleteMapping("/{news_id}")
    public ResponseEntity<NewsCommandResponse> deleteNewsById(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("news_id") Long newsId
    ) {
        NewsCommandResponse newsCommandResponse = newsCommandService.deleteNewsById(siteUser, newsId);
        return ResponseEntity.ok(newsCommandResponse);
    }
}
