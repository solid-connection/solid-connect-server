package com.example.solidconnection.siteuser.controller;

import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.siteuser.dto.NicknameExistsResponse;
import com.example.solidconnection.siteuser.dto.UserBlockResponse;
import com.example.solidconnection.siteuser.service.SiteUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/users")
@RestController
public class SiteUserController {

    private final SiteUserService siteUserService;

    @GetMapping("/exists")
    public ResponseEntity<NicknameExistsResponse> checkNicknameExists(
            @RequestParam("nickname") String nickname
    ) {
        NicknameExistsResponse nicknameExistsResponse = siteUserService.checkNicknameExists(nickname);
        return ResponseEntity.ok(nicknameExistsResponse);
    }

    @GetMapping("/blocks")
    public ResponseEntity<SliceResponse<UserBlockResponse>> getBlockedUsers(
            @AuthorizedUser long siteUserId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        SliceResponse<UserBlockResponse> response = siteUserService.getBlockedUsers(siteUserId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/block/{blocked-id}")
    public ResponseEntity<Void> blockUser(
            @AuthorizedUser long siteUserId,
            @PathVariable("blocked-id") Long blockedId
    ) {
        siteUserService.blockUser(siteUserId, blockedId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/block/{blocked-id}")
    public ResponseEntity<Void> cancelUserBlock(
            @AuthorizedUser long siteUserId,
            @PathVariable("blocked-id") Long blockedId
    ) {
        siteUserService.cancelUserBlock(siteUserId, blockedId);
        return ResponseEntity.ok().build();
    }
}
