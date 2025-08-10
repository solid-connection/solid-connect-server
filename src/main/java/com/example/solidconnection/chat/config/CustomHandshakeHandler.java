package com.example.solidconnection.chat.config;

import java.security.Principal;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

// WebSocket 세션의 Principal을 결정한다.
@Component
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        Object userAttribute = attributes.get("user");

        if (userAttribute instanceof Principal) {
            Principal principal = (Principal) userAttribute;
            return principal;
        }

        return super.determineUser(request, wsHandler, attributes);
    }
}
