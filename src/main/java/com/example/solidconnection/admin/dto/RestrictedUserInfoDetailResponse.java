package com.example.solidconnection.admin.dto;

import java.util.List;

public record RestrictedUserInfoDetailResponse(
	List<ReportedHistoryResponse> reportedHistoryResponses,
	List<BannedHistoryResponse> bannedHistoryResponses
) {

}
