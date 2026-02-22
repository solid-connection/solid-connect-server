package com.example.solidconnection.admin.dto;

import java.util.List;

public record MenteeInfoResponse(
	UnivApplyInfoResponse univApplyInfos,
	List<MatchedInfoResponse> mentorInfos
) {

}
