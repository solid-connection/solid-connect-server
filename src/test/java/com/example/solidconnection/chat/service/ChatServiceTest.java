package com.example.solidconnection.chat.service;

import static com.example.solidconnection.common.exception.ErrorCode.CHAT_PARTNER_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.CHAT_ROOM_ACCESS_DENIED;
import static com.example.solidconnection.common.exception.ErrorCode.INVALID_CHAT_ROOM_STATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.chat.domain.ChatAttachment;
import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.domain.ChatParticipant;
import com.example.solidconnection.chat.domain.ChatRoom;
import com.example.solidconnection.chat.dto.ChatMessageResponse;
import com.example.solidconnection.chat.dto.ChatRoomListResponse;
import com.example.solidconnection.chat.fixture.ChatAttachmentFixture;
import com.example.solidconnection.chat.fixture.ChatMessageFixture;
import com.example.solidconnection.chat.fixture.ChatParticipantFixture;
import com.example.solidconnection.chat.fixture.ChatReadStatusFixture;
import com.example.solidconnection.chat.fixture.ChatRoomFixture;
import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@TestContainerSpringBootTest
@DisplayName("채팅 서비스 테스트")
class ChatServiceTest {

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

    @Autowired
    private ChatReadStatusFixture chatReadStatusFixture;

    @Autowired
    private ChatAttachmentFixture chatAttachmentFixture;

    private SiteUser user;
    private SiteUser mentor1;
    private SiteUser mentor2;

    @BeforeEach
    void setUp() {
        user = siteUserFixture.사용자();
        mentor1 = siteUserFixture.사용자(1, "mentor1");
        mentor2 = siteUserFixture.사용자(2, "mentor2");
    }

    @Nested
    class 채팅방_목록을_조회한다 {

        @Test
        void 채팅방이_없으면_빈_목록을_반환한다() {
            // when
            ChatRoomListResponse response = chatService.getChatRooms(user.getId());

            // then
            assertThat(response.chatRooms()).isEmpty();
        }

        @Test
        void 최신_메시지_순으로_정렬되어_조회한다() {
            // given
            ChatRoom chatRoom1 = chatRoomFixture.채팅방(false);
            chatParticipantFixture.참여자(user.getId(), chatRoom1);
            chatParticipantFixture.참여자(mentor1.getId(), chatRoom1);
            ChatMessage oldMessage = chatMessageFixture.메시지("오래된 메시지", mentor1.getId(), chatRoom1);

            ChatRoom chatRoom2 = chatRoomFixture.채팅방(false);
            chatParticipantFixture.참여자(user.getId(), chatRoom2);
            chatParticipantFixture.참여자(mentor2.getId(), chatRoom2);
            ChatMessage newMessage = chatMessageFixture.메시지("최신 메시지", mentor2.getId(), chatRoom2);

            // when
            ChatRoomListResponse response = chatService.getChatRooms(user.getId());

            // then
            assertAll(
                    () -> assertThat(response.chatRooms()).hasSize(2),
                    () -> assertThat(response.chatRooms().get(0).partner().partnerId()).isEqualTo(mentor2.getId()),
                    () -> assertThat(response.chatRooms().get(0).lastChatMessage()).isEqualTo(newMessage.getContent()),
                    () -> assertThat(response.chatRooms().get(1).partner().partnerId()).isEqualTo(mentor1.getId()),
                    () -> assertThat(response.chatRooms().get(1).lastChatMessage()).isEqualTo(oldMessage.getContent()
                    ));
        }

        @Test
        void 그룹_채팅방은_제외하고_1대1_채팅방만_조회한다() {
            // given
            ChatRoom oneOnOneRoom = chatRoomFixture.채팅방(false);
            chatParticipantFixture.참여자(user.getId(), oneOnOneRoom);
            chatParticipantFixture.참여자(mentor1.getId(), oneOnOneRoom);

            ChatRoom groupRoom = chatRoomFixture.채팅방(true);
            chatParticipantFixture.참여자(user.getId(), groupRoom);
            chatParticipantFixture.참여자(mentor1.getId(), groupRoom);
            chatParticipantFixture.참여자(mentor2.getId(), groupRoom);

            // when
            ChatRoomListResponse response = chatService.getChatRooms(user.getId());

            // then
            assertAll(
                    () -> assertThat(response.chatRooms()).hasSize(1),
                    () -> assertThat(response.chatRooms().get(0).id()).isEqualTo(oneOnOneRoom.getId())
            );
        }

        @Test
        void 채팅_상대방이_없으면_예외가_발생한다() {
            // given
            ChatRoom chatRoom = chatRoomFixture.채팅방(false);
            chatParticipantFixture.참여자(user.getId(), chatRoom);

            // when & then
            assertThatCode(() -> chatService.getChatRooms(user.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(CHAT_PARTNER_NOT_FOUND.getMessage());
        }

        @Test
        void 일대일_채팅방에_참여자가_3명_이상이면_예외가_발생한다() {
            // given
            ChatRoom chatRoom = chatRoomFixture.채팅방(false);
            chatParticipantFixture.참여자(user.getId(), chatRoom);
            chatParticipantFixture.참여자(mentor1.getId(), chatRoom);
            chatParticipantFixture.참여자(mentor2.getId(), chatRoom);

            // when & then
            assertThatCode(() -> chatService.getChatRooms(user.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_CHAT_ROOM_STATE.getMessage());
        }
    }

    @Nested
    class 읽지_않은_메시지_수를_조회한다 {

        private ChatRoom chatRoom;
        private ChatParticipant participant;

        @BeforeEach
        void setUp() {
            chatRoom = chatRoomFixture.채팅방(false);
            participant = chatParticipantFixture.참여자(user.getId(), chatRoom);
            chatParticipantFixture.참여자(mentor1.getId(), chatRoom);
        }

        @Test
        void 읽음_상태가_없으면_모든_상대방_메시지를_카운팅한다() {
            // given
            chatMessageFixture.메시지("메시지1", mentor1.getId(), chatRoom);
            chatMessageFixture.메시지("메시지2", mentor1.getId(), chatRoom);

            // when
            ChatRoomListResponse response = chatService.getChatRooms(user.getId());

            // then
            assertThat(response.chatRooms().get(0).unReadCount()).isEqualTo(2);
        }

        @Test
        void 읽음_상태_이후_메시지만_읽지_않은_메시지로_카운팅한다() {
            // given
            chatMessageFixture.메시지("읽은 메시지", mentor1.getId(), chatRoom);
            chatReadStatusFixture.읽음상태(chatRoom.getId(), participant.getId());

            chatMessageFixture.메시지("읽지 않은 메시지1", mentor1.getId(), chatRoom);
            chatMessageFixture.메시지("읽지 않은 메시지2", mentor1.getId(), chatRoom);

            // when
            ChatRoomListResponse response = chatService.getChatRooms(user.getId());

            // then
            assertThat(response.chatRooms().get(0).unReadCount()).isEqualTo(2);
        }
    }

    @Nested
    class 채팅_메시지를_조회한다 {

        private static final int NO_NEXT_PAGE_NUMBER = -1;

        private ChatRoom chatRoom;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
            chatRoom = chatRoomFixture.채팅방(false);
            chatParticipantFixture.참여자(user.getId(), chatRoom);
            chatParticipantFixture.참여자(mentor1.getId(), chatRoom);

            pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        @Test
        void 메시지가_없는_채팅방에서_빈_목록을_반환한다() {
            // when
            SliceResponse<ChatMessageResponse> response = chatService.getChatMessages(user.getId(), chatRoom.getId(), pageable);

            // then
            assertAll(
                    () -> assertThat(response.content()).isEmpty(),
                    () -> assertThat(response.nextPageNumber()).isEqualTo(NO_NEXT_PAGE_NUMBER)
            );
        }

        @Test
        void 첨부파일이_없는_메시지들을_정상_조회한다() {
            // given
            ChatMessage message1 = chatMessageFixture.메시지("메시지1", mentor1.getId(), chatRoom);
            ChatMessage message2 = chatMessageFixture.메시지("메시지2", user.getId(), chatRoom);

            // when
            SliceResponse<ChatMessageResponse> response = chatService.getChatMessages(user.getId(), chatRoom.getId(), pageable);

            // then
            assertAll(
                    () -> assertThat(response.content()).hasSize(2),
                    () -> assertThat(response.content().get(0).content()).isEqualTo(message2.getContent()),
                    () -> assertThat(response.content().get(0).senderId()).isEqualTo(user.getId()),
                    () -> assertThat(response.content().get(1).content()).isEqualTo(message1.getContent()),
                    () -> assertThat(response.content().get(1).senderId()).isEqualTo(mentor1.getId())
            );
        }

        @Test
        void 첨부파일이_있는_메시지를_정상_조회한다() {
            // given
            ChatMessage messageWithImage = chatMessageFixture.메시지("이미지", mentor1.getId(), chatRoom);
            ChatAttachment imageAttachment = chatAttachmentFixture.첨부파일(
                    true,
                    "https://example.com/image.png",
                    "https://example.com/thumb.png",
                    messageWithImage
            );

            // when
            SliceResponse<ChatMessageResponse> response = chatService.getChatMessages(user.getId(), chatRoom.getId(), pageable);

            // then
            assertAll(
                    () -> assertThat(response.content()).hasSize(1),
                    () -> assertThat(response.content().get(0).content()).isEqualTo(messageWithImage.getContent()),
                    () -> assertThat(response.content().get(0).attachments()).hasSize(1),
                    () -> assertThat(response.content().get(0).attachments().get(0).id()).isEqualTo(imageAttachment.getId())
            );
        }

        @Test
        void 페이징이_정상_작동한다() {
            for (int i = 1; i <= 25; i++) {
                chatMessageFixture.메시지("메시지" + i, (i % 2 == 0) ? user.getId() : mentor1.getId(), chatRoom);
            }

            Pageable firstPage = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            Pageable secondPage = PageRequest.of(1, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            SliceResponse<ChatMessageResponse> firstResponse = chatService.getChatMessages(user.getId(), chatRoom.getId(), firstPage);
            SliceResponse<ChatMessageResponse> secondResponse = chatService.getChatMessages(user.getId(), chatRoom.getId(), secondPage);

            // then
            assertAll(
                    () -> assertThat(firstResponse.nextPageNumber()).isEqualTo(2),
                    () -> assertThat(secondResponse.nextPageNumber()).isEqualTo(NO_NEXT_PAGE_NUMBER)
            );
        }

        @Test
        void 채팅방_참여자가_아니면_예외가_발생한다() {
            // when & then
            assertThatCode(() -> chatService.getChatMessages(mentor2.getId(), chatRoom.getId(), pageable))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(CHAT_ROOM_ACCESS_DENIED.getMessage());
        }

        @Test
        void 존재하지_않는_채팅방에_접근하면_예외가_발생한다() {
            // given
            long nonExistentRoomId = 999L;

            // when & then
            assertThatCode(() -> chatService.getChatMessages(user.getId(), nonExistentRoomId, pageable))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(CHAT_ROOM_ACCESS_DENIED.getMessage());
        }
    }
}
