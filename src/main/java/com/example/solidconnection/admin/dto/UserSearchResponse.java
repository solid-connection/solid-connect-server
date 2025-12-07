package com.example.solidconnection.admin.dto;

import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.UserStatus;

public record UserSearchResponse(
	String nickname,
	String email,
	Role role,
	UserStatus userStatus
) {
}
