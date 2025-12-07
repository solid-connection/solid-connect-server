package com.example.solidconnection.admin.dto;

import java.util.List;

public record RestrictedUserInfoDetailResponse(
	List<ReportedHistoryResponse> reportedHistoryResponses, // ACTIVE 유저일 경우 빈 리스트
	List<BannedHistoryResponse> bannedHistoryResponses // ACTIVE, REPORTED 유저일 경우 빈 리스트
) {
}
