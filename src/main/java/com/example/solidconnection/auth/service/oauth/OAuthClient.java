package com.example.solidconnection.auth.service.oauth;

import com.example.solidconnection.auth.dto.oauth.OAuthUserInfoDto;
import com.example.solidconnection.siteuser.domain.AuthType;

public interface OAuthClient {

    OAuthUserInfoDto getUserInfo(String code);

    AuthType getAuthType();
}
