package com.example.solidconnection.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.solidconnection.admin.dto.UserBanRequest;
import com.example.solidconnection.admin.service.AdminUserBanService;
import com.example.solidconnection.common.resolver.AuthorizedUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@RequestMapping("/admin/users")
@RestController
@Slf4j
public class AdminUserBanController {
	private final AdminUserBanService adminUserBanService;

	@PostMapping("/{userId}/ban")
	public ResponseEntity<Void> banUser(
            @PathVariable long userId,
		    @Valid @RequestBody UserBanRequest request
	) {
		adminUserBanService.banUser(userId, request);
		return ResponseEntity.ok().build();
	}

    @PatchMapping("/{userId}/unban")
    public ResponseEntity<Void> unbanUser(
            @AuthorizedUser long adminId,
            @PathVariable long userId
    ) {
        adminUserBanService.unbanUser(userId, adminId);
        return ResponseEntity.ok().build();
    }
}
