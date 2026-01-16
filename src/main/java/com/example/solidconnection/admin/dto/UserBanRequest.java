package com.example.solidconnection.admin.dto;

import com.example.solidconnection.siteuser.domain.UserBanDuration;

import jakarta.validation.constraints.NotNull;

public record UserBanRequest(
    @NotNull(message = "차단 기간을 입력해주세요.")
	UserBanDuration duration
) {
}
