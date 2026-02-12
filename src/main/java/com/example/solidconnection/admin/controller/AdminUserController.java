package com.example.solidconnection.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.solidconnection.admin.dto.RestrictedUserInfoDetailResponse;
import com.example.solidconnection.admin.dto.RestrictedUserSearchCondition;
import com.example.solidconnection.admin.dto.RestrictedUserSearchResponse;
import com.example.solidconnection.admin.dto.UserInfoDetailResponse;
import com.example.solidconnection.admin.dto.UserSearchCondition;
import com.example.solidconnection.admin.dto.UserSearchResponse;
import com.example.solidconnection.admin.service.AdminUserService;
import com.example.solidconnection.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/admin/users")
@RestController
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<PageResponse<UserSearchResponse>> searchAllUsers(
            @Valid @ModelAttribute UserSearchCondition searchCondition,
            Pageable pageable
    ) {
        Page<UserSearchResponse> page = adminUserService.searchAllUsers(searchCondition, pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

    @GetMapping("/{user-id}")
    public ResponseEntity<UserInfoDetailResponse> getUserInfoDetail(
		    @PathVariable(name = "user-id") long userId
    ) {
        UserInfoDetailResponse response = adminUserService.getUserInfoDetail(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/restricted")
    public ResponseEntity<PageResponse<RestrictedUserSearchResponse>> searchRestrictedUsers(
            @Valid @ModelAttribute RestrictedUserSearchCondition searchCondition,
            Pageable pageable
    ) {
        Page<RestrictedUserSearchResponse> page = adminUserService.searchRestrictedUsers(searchCondition, pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

    @GetMapping("/restricted/{user-id}")
    public ResponseEntity<RestrictedUserInfoDetailResponse> getRestrictedUserInfoDetail(
		    @PathVariable(name = "user-id") long userId
    ) {
        RestrictedUserInfoDetailResponse response = adminUserService.getRestrictedUserInfoDetail(userId);
        return ResponseEntity.ok(response);
    }
}
