package com.example.solidconnection.siteuser.dto;

import java.time.ZonedDateTime;

public record UserBlockResponse(
        long id,
        long blockedId,
        String nickname,
        ZonedDateTime createdAt
) {

}
