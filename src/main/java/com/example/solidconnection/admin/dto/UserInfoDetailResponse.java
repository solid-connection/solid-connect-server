package com.example.solidconnection.admin.dto;

import java.util.List;

public record UserInfoDetailResponse(
	MentorInfoResponse mentorInfoResponse, // 멘티일 경우 null
	MenteeInfoResponse menteeInfoResponse, // 멘토일 경우 null
	List<ReportedHistoryResponse> reportedHistoryResponses
) {

}
