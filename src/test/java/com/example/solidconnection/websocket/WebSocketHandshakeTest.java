package com.example.solidconnection.websocket;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.example.solidconnection.auth.domain.AccessToken;
import com.example.solidconnection.auth.service.AuthTokenProvider;
import com.example.solidconnection.chat.domain.ChatRoom;
import com.example.solidconnection.chat.fixture.ChatParticipantFixture;
import com.example.solidconnection.chat.fixture.ChatRoomFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@TestContainerSpringBootTest
@DisplayName("WebSocket Handshake 테스트")
class WebSocketHandshakeTest {

    @LocalServerPort
    private int port;
    private String url;
    private WebSocketStompClient stompClient;
    private StompSession stompSession;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private ChatRoomFixture chatRoomFixture;

    @Autowired
    private ChatParticipantFixture chatParticipantFixture;

    @BeforeEach
    void setUp() {
        this.url = String.format("http://localhost:%d/connect", port);
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

        @Test
        void 인증된_사용자는_핸드셰이크를_성공한다() throws Exception {
            // given
            SiteUser user = siteUserFixture.사용자();
            AccessToken accessToken = authTokenProvider.generateAccessToken(user);
            String tokenUrl = url + "?token=" + accessToken.token();

            // when
            stompSession = stompClient.connectAsync(tokenUrl, new StompSessionHandlerAdapter() {
            }).get(5, SECONDS);

            // then
            assertThat(stompSession.isConnected()).isTrue();
        }

        @Test
        void 연결_후_구독하지_않으면_제한시간_뒤_연결을_종료한다() throws Exception {
            // given
            SiteUser user = siteUserFixture.사용자();
            AccessToken accessToken = authTokenProvider.generateAccessToken(user);
            String tokenUrl = url + "?token=" + accessToken.token();
            stompSession = stompClient.connectAsync(tokenUrl, new StompSessionHandlerAdapter() {
            }).get(5, SECONDS);

            // when & then
            await().atMost(Duration.ofSeconds(3))
                    .untilAsserted(() -> assertThat(stompSession.isConnected()).isFalse());
        }

        @Test
        void 연결_후_제한시간_안에_구독하면_연결을_유지한다() throws Exception {
            // given
            SiteUser user = siteUserFixture.사용자();
            ChatRoom chatRoom = chatRoomFixture.채팅방(false);
            chatParticipantFixture.참여자(user.getId(), chatRoom);
            AccessToken accessToken = authTokenProvider.generateAccessToken(user);
            String tokenUrl = url + "?token=" + accessToken.token();
            stompSession = stompClient.connectAsync(tokenUrl, new StompSessionHandlerAdapter() {
            }).get(5, SECONDS);

            // when
            stompSession.subscribe("/topic/chat/" + chatRoom.getId(), new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Object.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                }
            });

            // then
            await().during(Duration.ofSeconds(1))
                    .atMost(Duration.ofSeconds(2))
                    .untilAsserted(() -> assertThat(stompSession.isConnected()).isTrue());
        }

        @Test
        void 인증되지_않은_사용자는_핸드셰이크를_실패한다() {
            // when & then
            assertThatThrownBy(() -> {
                stompClient.connectAsync(url, new StompSessionHandlerAdapter() {
                }).get(5, TimeUnit.SECONDS);
            }).isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(HttpClientErrorException.Unauthorized.class);
        }
    }
}
