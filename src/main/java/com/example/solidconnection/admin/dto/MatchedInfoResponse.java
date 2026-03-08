package com.example.solidconnection.admin.dto;

import java.time.ZonedDateTime;

public record MatchedInfoResponse(
	String nickname,
	ZonedDateTime matchedDate
) {

}
