package com.example.solidconnection.chat.service;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MICROS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.domain.ChatMessageOutbox;
import com.example.solidconnection.chat.domain.ChatMessageOutboxCompletionType;
import com.example.solidconnection.chat.domain.ChatMessageOutboxStatus;
import com.example.solidconnection.chat.domain.ChatParticipant;
import com.example.solidconnection.chat.domain.ChatRoom;
import com.example.solidconnection.chat.dto.ChatMessageSendResponse;
import com.example.solidconnection.chat.fixture.ChatMessageFixture;
import com.example.solidconnection.chat.fixture.ChatParticipantFixture;
import com.example.solidconnection.chat.fixture.ChatRoomFixture;
import com.example.solidconnection.chat.repository.ChatMessageOutboxRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@TestContainerSpringBootTest
@DisplayName("채팅 메시지 Outbox 디스패처 테스트")
class ChatMessageOutboxDispatcherTest {

    @Autowired
    private ChatMessageOutboxDispatcher dispatcher;

    @Autowired
    private ChatMessageOutboxRepository outboxRepository;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private ChatRoomFixture chatRoomFixture;

    @Autowired
    private ChatParticipantFixture chatParticipantFixture;

    @Autowired
    private ChatMessageFixture chatMessageFixture;

    @MockitoBean
    private SimpMessagingTemplate simpMessagingTemplate;

    private ChatMessageOutbox outbox;
    private long roomId;

    @BeforeEach
    void setUp() {
        SiteUser senderUser = siteUserFixture.사용자(1, "sender");
        SiteUser recipientUser = siteUserFixture.사용자(2, "recipient");
        ChatRoom chatRoom = chatRoomFixture.채팅방(false);
        roomId = chatRoom.getId();
        ChatParticipant sender = chatParticipantFixture.참여자(senderUser.getId(), chatRoom);
        ChatParticipant recipient = chatParticipantFixture.참여자(recipientUser.getId(), chatRoom);
        ChatMessage message = chatMessageFixture.메시지("메시지", sender.getId(), chatRoom);
        ZonedDateTime now = ZonedDateTime.now(UTC).truncatedTo(MICROS);
        outbox = outboxRepository.save(new ChatMessageOutbox(message, recipient, now.minusSeconds(1)));
    }

    @Nested
    class 메시지를_방송한다 {

        @Test
        void DB_트랜잭션_밖에서_방송하고_성공_상태를_별도_커밋한다() {
            // given
            BDDMockito.willAnswer(invocation -> {
                assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
                return null;
            }).given(simpMessagingTemplate).convertAndSend(
                    BDDMockito.anyString(),
                    BDDMockito.any(ChatMessageSendResponse.class)
            );

            // when
            dispatcher.dispatch(outbox.getId());

            // then
            ArgumentCaptor<ChatMessageSendResponse> responseCaptor =
                    ArgumentCaptor.forClass(ChatMessageSendResponse.class);
            BDDMockito.then(simpMessagingTemplate).should().convertAndSend(
                    BDDMockito.eq("/topic/chat/" + roomId),
                    responseCaptor.capture()
            );
            ChatMessageOutbox saved = outboxRepository.findById(outbox.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(responseCaptor.getValue().messageId())
                            .isEqualTo(outbox.getChatMessage().getId()),
                    () -> assertThat(saved.getStatus()).isEqualTo(ChatMessageOutboxStatus.COMPLETED),
                    () -> assertThat(saved.getCompletionType())
                            .isEqualTo(ChatMessageOutboxCompletionType.BROKER_DISPATCHED)
            );
        }

        @Test
        void 방송에_실패하면_예외를_전파하지_않고_재시도를_예약한다() {
            // given
            BDDMockito.willThrow(new RuntimeException("broadcast failed"))
                    .given(simpMessagingTemplate)
                    .convertAndSend(BDDMockito.anyString(), BDDMockito.any(ChatMessageSendResponse.class));

            // when & then
            assertThatCode(() -> dispatcher.dispatch(outbox.getId())).doesNotThrowAnyException();

            ChatMessageOutbox saved = outboxRepository.findById(outbox.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(saved.getStatus()).isEqualTo(ChatMessageOutboxStatus.PENDING),
                    () -> assertThat(saved.getAttemptCount()).isEqualTo(1),
                    () -> assertThat(saved.getLastError()).contains("broadcast failed")
            );
        }
    }
}
