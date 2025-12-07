package com.example.solidconnection.admin.dto;

import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.UserStatus;

public record RestrictedUserSearchResponse(
	String nickname,
	Role role,
	UserStatus userStatus,
	ReportedInfoResponse reportedInfoResponse,
	BannedInfoResponse bannedInfoResponse
) {
}
