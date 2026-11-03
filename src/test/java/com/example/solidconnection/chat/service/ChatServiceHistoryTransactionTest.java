package com.example.solidconnection.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;

import com.example.solidconnection.chat.domain.ChatParticipant;
import com.example.solidconnection.chat.domain.ChatRoom;
import com.example.solidconnection.chat.fixture.ChatMessageFixture;
import com.example.solidconnection.chat.fixture.ChatParticipantFixture;
import com.example.solidconnection.chat.fixture.ChatRoomFixture;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@TestContainerSpringBootTest
@DisplayName("채팅 내역 조회 트랜잭션 테스트")
class ChatServiceHistoryTransactionTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private ChatRoomFixture chatRoomFixture;

    @Autowired
    private ChatParticipantFixture chatParticipantFixture;

    @Autowired
    private ChatMessageFixture chatMessageFixture;

    @MockitoBean
    private ChatMessageOutboxTransactionService outboxTransactionService;

    @Test
    void 조회_트랜잭션을_종료한_뒤_Outbox_완료를_기록한다() {
        // given
        SiteUser user = siteUserFixture.사용자(1, "user");
        SiteUser partner = siteUserFixture.사용자(2, "partner");
        ChatRoom chatRoom = chatRoomFixture.채팅방(false);
        ChatParticipant userParticipant = chatParticipantFixture.참여자(user.getId(), chatRoom);
        ChatParticipant partnerParticipant = chatParticipantFixture.참여자(partner.getId(), chatRoom);
        chatMessageFixture.메시지("메시지", partnerParticipant.getId(), chatRoom);
        given(outboxTransactionService.markHistoryServed(
                org.mockito.ArgumentMatchers.eq(userParticipant.getId()),
                org.mockito.ArgumentMatchers.anyList(),
                org.mockito.ArgumentMatchers.any()
        )).willAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            return 1;
        });

        // when
        chatService.getChatMessages(user.getId(), chatRoom.getId(), PageRequest.of(0, 20));

        // then
        org.mockito.BDDMockito.then(outboxTransactionService).should().markHistoryServed(
                org.mockito.ArgumentMatchers.eq(userParticipant.getId()),
                org.mockito.ArgumentMatchers.anyList(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void Outbox_완료_기록에_실패해도_조회_응답은_반환한다() {
        // given
        SiteUser user = siteUserFixture.사용자(1, "user");
        SiteUser partner = siteUserFixture.사용자(2, "partner");
        ChatRoom chatRoom = chatRoomFixture.채팅방(false);
        ChatParticipant userParticipant = chatParticipantFixture.참여자(user.getId(), chatRoom);
        ChatParticipant partnerParticipant = chatParticipantFixture.참여자(partner.getId(), chatRoom);
        chatMessageFixture.메시지("메시지", partnerParticipant.getId(), chatRoom);
        given(outboxTransactionService.markHistoryServed(
                org.mockito.ArgumentMatchers.eq(userParticipant.getId()),
                org.mockito.ArgumentMatchers.anyList(),
                org.mockito.ArgumentMatchers.any()
        )).willThrow(new RuntimeException("state update failed"));

        // when & then
        assertThatCode(() -> chatService.getChatMessages(
                user.getId(),
                chatRoom.getId(),
                PageRequest.of(0, 20)
        )).doesNotThrowAnyException();
    }
}
