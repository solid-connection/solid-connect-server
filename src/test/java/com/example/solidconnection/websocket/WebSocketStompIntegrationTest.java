package com.example.solidconnection.websocket;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.auth.service.AccessToken;
import com.example.solidconnection.auth.service.AuthTokenProvider;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@TestContainerSpringBootTest
@DisplayName("WebSocket/STOMP 통합 테스트")
class WebSocketStompIntegrationTest {

    @LocalServerPort
    private int port;
    private String url;
    private WebSocketStompClient stompClient;
    private StompSession stompSession;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @BeforeEach
    void setUp() {
        this.url = String.format("ws://localhost:%d/connect", port);
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        this.stompClient = new WebSocketStompClient(new SockJsClient(transports));
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @AfterEach
    void tearDown() {
        if (this.stompSession != null && this.stompSession.isConnected()) {
            this.stompSession.disconnect();
        }
    }

    @Nested
    class WebSocket_핸드셰이크_및_STOMP_세션_수립_테스트 {

        private final BlockingQueue<Throwable> transportErrorQueue = new ArrayBlockingQueue<>(1);

        private final StompSessionHandlerAdapter sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                transportErrorQueue.add(exception);
            }
        };

        @Test
        void 인증된_사용자는_핸드셰이크를_성공한다() throws Exception {
            // given
            SiteUser user = siteUserFixture.사용자();
            AccessToken accessToken = authTokenProvider.generateAccessToken(authTokenProvider.toSubject(user), user.getRole());

            WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
            handshakeHeaders.add("Authorization", "Bearer " + accessToken.token());

            // when
            stompSession = stompClient.connectAsync(url, handshakeHeaders, new StompHeaders(), sessionHandler).get(5, SECONDS);

            // then
            assertAll(
                () -> assertThat(stompSession).isNotNull(),
                () -> assertThat(transportErrorQueue).isEmpty()
            );
        }

        @Test
        void 인증되지_않은_사용자는_핸드셰이크를_실패한다() {
            // when
            Throwable thrown = catchThrowable(() -> {
                stompSession = stompClient.connectAsync(url, new WebSocketHttpHeaders(), new StompHeaders(), sessionHandler).get(5, SECONDS);
            });

            // then
            assertAll(
                    () -> assertThat(thrown)
                            .isInstanceOf(ExecutionException.class)
                            .hasCauseInstanceOf(HttpClientErrorException.Unauthorized.class),
                    () -> assertThat(transportErrorQueue).hasSize(1)
            );
        }
    }
}
