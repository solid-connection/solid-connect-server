package com.example.solidconnection.admin.dto;

import com.example.solidconnection.siteuser.domain.UserBanDuration;
import com.fasterxml.jackson.annotation.JsonProperty;

public record BannedInfoResponse(
		@JsonProperty("isBanned") boolean isBanned,
		UserBanDuration duration
) {

}
