package com.example.solidconnection.admin.auth.dto;

import com.example.solidconnection.auth.domain.AccessToken;

public record AdminReissueResponse(
        String accessToken
) {

    public static AdminReissueResponse from(AccessToken accessToken) {
        return new AdminReissueResponse(accessToken.token());
    }
}
