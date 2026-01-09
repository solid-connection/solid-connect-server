package com.example.solidconnection.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.solidconnection.admin.dto.UserBanRequest;
import com.example.solidconnection.admin.service.AdminUserBanService;
import com.example.solidconnection.common.resolver.AuthorizedUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/admin/users")
@RestController
public class AdminUserBanController {
	private final AdminUserBanService adminUserBanService;

	@PostMapping("/{user-id}/ban")
	public ResponseEntity<Void> banUser(
			@AuthorizedUser long adminId,
			@PathVariable(name = "user-id") long userId,
			@Valid @RequestBody UserBanRequest request
	) {
		adminUserBanService.banUser(userId, adminId, request);
		return ResponseEntity.ok().build();
	}

    @PatchMapping("/{user-id}/unban")
    public ResponseEntity<Void> unbanUser(
            @AuthorizedUser long adminId,
            @PathVariable(name = "user-id") long userId
    ) {
        adminUserBanService.unbanUser(userId, adminId);
        return ResponseEntity.ok().build();
    }
}
