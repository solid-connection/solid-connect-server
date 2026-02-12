package com.example.solidconnection.admin.dto;

import com.example.solidconnection.siteuser.domain.UserBanDuration;

public record BannedInfoResponse(
	boolean isBanned,
	UserBanDuration duration
) {

}
