package com.example.solidconnection.auth.dto;

import com.example.solidconnection.auth.domain.AccessToken;

public record ReissueResponse(
        String accessToken
) {

    public static ReissueResponse from(AccessToken accessToken) {
        return new ReissueResponse(accessToken.token());
    }
}
