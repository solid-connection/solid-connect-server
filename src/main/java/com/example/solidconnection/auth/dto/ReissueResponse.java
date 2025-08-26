package com.example.solidconnection.auth.dto;

import com.example.solidconnection.auth.service.AccessToken;

public record ReissueResponse(
        String accessToken
) {

    public static ReissueResponse from(AccessToken accessToken) {
        return new ReissueResponse(accessToken.token());
    }
}
