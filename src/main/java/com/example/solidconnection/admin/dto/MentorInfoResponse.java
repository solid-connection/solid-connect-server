package com.example.solidconnection.admin.dto;

import java.util.List;

public record MentorInfoResponse(
	List<MatchedInfoResponse> menteeInfos,
	List<MentorApplicationHistoryResponse> mentorApplicationHistory
) {
}
