package com.example.solidconnection.auth.service.oauth;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.siteuser.domain.AuthType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OAuthClientMap {

    private final Map<AuthType, OAuthClient> oauthClientMap;

    public OAuthClientMap(List<OAuthClient> oAuthClientList) {
        this.oauthClientMap = oAuthClientList.stream()
                .collect(Collectors.toMap(OAuthClient::getAuthType, Function.identity()));
    }

    public OAuthClient getOAuthClient(AuthType authType) {
        OAuthClient oauthClient = oauthClientMap.get(authType);
        if (oauthClient == null) {
            throw new CustomException(
                    ErrorCode.NOT_DEFINED_ERROR,
                    "처리할 수 있는 OAuthClient가 없습니다. authType: " + authType.name()
            );
        }
        return oauthClient;
    }
}
