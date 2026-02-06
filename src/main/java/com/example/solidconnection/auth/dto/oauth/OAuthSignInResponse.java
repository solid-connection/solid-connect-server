package com.example.solidconnection.auth.dto.oauth;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record OAuthSignInResponse(
        boolean isRegistered,
        String accessToken,
        @JsonIgnore String refreshToken) implements OAuthResponse {

}
