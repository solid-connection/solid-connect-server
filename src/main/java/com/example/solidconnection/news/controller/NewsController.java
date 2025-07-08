package com.example.solidconnection.news.controller;

import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.news.dto.LikedNewsResponse;
import com.example.solidconnection.news.dto.NewsCommandResponse;
import com.example.solidconnection.news.dto.NewsCreateRequest;
import com.example.solidconnection.news.dto.NewsListResponse;
import com.example.solidconnection.news.dto.NewsUpdateRequest;
import com.example.solidconnection.news.service.NewsCommandService;
import com.example.solidconnection.news.service.NewsLikeService;
import com.example.solidconnection.news.service.NewsQueryService;
import com.example.solidconnection.security.annotation.RequireRoleAccess;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final NewsLikeService newsLikeService;

    // todo: 추후 Slice 적용
    @GetMapping
    public ResponseEntity<NewsListResponse> findNewsBySiteUserId(
            @RequestParam(value = "site-user-id") Long siteUserId
    ) {
        NewsListResponse newsListResponse = newsQueryService.findNewsBySiteUserId(siteUserId);
        return ResponseEntity.ok(newsListResponse);
    }

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
    @PutMapping("/{news-id}")
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
    @DeleteMapping("/{news-id}")
    public ResponseEntity<NewsCommandResponse> deleteNewsById(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("news-id") Long newsId
    ) {
        NewsCommandResponse newsCommandResponse = newsCommandService.deleteNewsById(siteUser, newsId);
        return ResponseEntity.ok(newsCommandResponse);
    }

    @GetMapping("/{news-id}/like")
    public ResponseEntity<LikedNewsResponse> getNewsLikeStatus(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("news-id") Long newsId
    ) {
        LikedNewsResponse likedNewsResponse = newsLikeService.getNewsLikeStatus(siteUser.getId(), newsId);
        return ResponseEntity.ok(likedNewsResponse);
    }

    @PostMapping("/{news-id}/like")
    public void addNewsLike(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("news-id") Long newsId
    ) {
        newsLikeService.addNewsLike(siteUser.getId(), newsId);
    }

    @DeleteMapping("/{news-id}/like")
    public void cancelNewsLike(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("news-id") Long newsId
    ) {
        newsLikeService.cancelNewsLike(siteUser.getId(), newsId);
    }
}
