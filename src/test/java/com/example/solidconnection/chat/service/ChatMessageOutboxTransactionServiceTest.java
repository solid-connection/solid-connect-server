package com.example.solidconnection.chat.service;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MICROS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.solidconnection.chat.domain.ChatMessage;
import com.example.solidconnection.chat.domain.ChatMessageOutbox;
import com.example.solidconnection.chat.domain.ChatMessageOutboxCompletionType;
import com.example.solidconnection.chat.domain.ChatMessageOutboxStatus;
import com.example.solidconnection.chat.domain.ChatParticipant;
import com.example.solidconnection.chat.domain.ChatRoom;
import com.example.solidconnection.chat.dto.ClaimedChatMessageOutbox;
import com.example.solidconnection.chat.fixture.ChatMessageFixture;
import com.example.solidconnection.chat.fixture.ChatMessageOutboxFixture;
import com.example.solidconnection.chat.fixture.ChatParticipantFixture;
import com.example.solidconnection.chat.fixture.ChatRoomFixture;
import com.example.solidconnection.chat.repository.ChatMessageOutboxRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@TestContainerSpringBootTest
@DisplayName("채팅 메시지 Outbox 트랜잭션 서비스 테스트")
class ChatMessageOutboxTransactionServiceTest {

    @Autowired
    private ChatMessageOutboxTransactionService outboxTransactionService;

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

    @Autowired
    private ChatMessageOutboxFixture chatMessageOutboxFixture;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ChatParticipant recipient;
    private ChatMessageOutbox outbox;
    private ZonedDateTime now;

    @BeforeEach
    void setUp() {
        SiteUser senderUser = siteUserFixture.사용자(1, "sender");
        SiteUser recipientUser = siteUserFixture.사용자(2, "recipient");
        ChatRoom chatRoom = chatRoomFixture.채팅방(false);
        ChatParticipant sender = chatParticipantFixture.참여자(senderUser.getId(), chatRoom);
        recipient = chatParticipantFixture.참여자(recipientUser.getId(), chatRoom);
        ChatMessage message = chatMessageFixture.메시지("메시지", sender.getId(), chatRoom);
        now = ZonedDateTime.now(UTC).truncatedTo(MICROS);
        outbox = chatMessageOutboxFixture.아웃박스(message, recipient, now.minusMinutes(10));
    }

    @Nested
    class 처리권을_획득한다 {

        @Test
        void 처리권을_획득하면_시도_횟수와_token을_기록한다() {
            // when
            ClaimedChatMessageOutbox claimed = outboxTransactionService.claim(outbox.getId(), now)
                    .orElseThrow();

            // then
            ChatMessageOutbox saved = outboxRepository.findById(outbox.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(saved.getStatus()).isEqualTo(ChatMessageOutboxStatus.PROCESSING),
                    () -> assertThat(saved.getAttemptCount()).isEqualTo(1),
                    () -> assertThat(saved.getProcessingToken()).isEqualTo(claimed.processingToken()),
                    () -> assertThat(claimed.response().messageId()).isEqualTo(outbox.getChatMessage().getId())
            );
        }

        @Test
        void 이미_처리권을_획득한_Outbox는_다시_획득할_수_없다() {
            // given
            outboxTransactionService.claim(outbox.getId(), now).orElseThrow();

            // when & then
            assertThat(outboxTransactionService.claim(outbox.getId(), now)).isEmpty();
        }

        @Test
        void 동일한_Outbox를_동시에_claim하면_하나의_worker만_처리권을_획득한다() throws Exception {
            // given
            CountDownLatch workersReady = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);

            try (var executor = Executors.newFixedThreadPool(2)) {
                try {
                    List<Future<Optional<ClaimedChatMessageOutbox>>> claims = List.of(
                            executor.submit(() -> claimAfterStart(workersReady, start)),
                            executor.submit(() -> claimAfterStart(workersReady, start))
                    );
                    boolean allWorkersReady = workersReady.await(5, TimeUnit.SECONDS);

                    // when
                    start.countDown();

                    // then
                    assertThat(allWorkersReady).isTrue();
                    List<Optional<ClaimedChatMessageOutbox>> claimResults = new ArrayList<>();
                    for (Future<Optional<ClaimedChatMessageOutbox>> claim : claims) {
                        claimResults.add(claim.get(10, TimeUnit.SECONDS));
                    }
                    long claimedCount = claimResults.stream().filter(Optional::isPresent).count();
                    ChatMessageOutbox saved = outboxRepository.findById(outbox.getId()).orElseThrow();
                    assertAll(
                            () -> assertThat(claimedCount).isEqualTo(1),
                            () -> assertThat(saved.getStatus()).isEqualTo(ChatMessageOutboxStatus.PROCESSING),
                            () -> assertThat(saved.getAttemptCount()).isEqualTo(1)
                    );
                } finally {
                    start.countDown();
                }
            }
        }

        private Optional<ClaimedChatMessageOutbox> claimAfterStart(
                CountDownLatch workersReady,
                CountDownLatch start
        ) throws InterruptedException {
            workersReady.countDown();
            start.await();
            return outboxTransactionService.claim(outbox.getId(), now);
        }

        @Test
        void 삭제되어_방송할_수_없는_메시지는_FAILED로_종료한다() {
            // given
            jdbcTemplate.update(
                    "UPDATE chat_message SET is_deleted = true WHERE id = ?",
                    outbox.getChatMessage().getId()
            );

            // when
            Optional<ClaimedChatMessageOutbox> claimed =
                    outboxTransactionService.claim(outbox.getId(), now);

            // then
            ChatMessageOutbox saved = outboxRepository.findById(outbox.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(claimed).isEmpty(),
                    () -> assertThat(saved.getStatus()).isEqualTo(ChatMessageOutboxStatus.FAILED),
                    () -> assertThat(saved.getAttemptCount()).isEqualTo(1),
                    () -> assertThat(saved.getLastError()).contains("message unavailable")
            );
        }
    }

    @Nested
    class 처리_결과를_기록한다 {

        @Test
        void 방송에_성공하면_BROKER_DISPATCHED로_완료한다() {
            // given
            ClaimedChatMessageOutbox claimed = outboxTransactionService.claim(outbox.getId(), now)
                    .orElseThrow();

            // when
            boolean updated = outboxTransactionService.markCompleted(
                    outbox.getId(),
                    claimed.processingToken(),
                    ChatMessageOutboxCompletionType.BROKER_DISPATCHED,
                    now.plusSeconds(1)
            );

            // then
            ChatMessageOutbox saved = outboxRepository.findById(outbox.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(updated).isTrue(),
                    () -> assertThat(saved.getStatus()).isEqualTo(ChatMessageOutboxStatus.COMPLETED),
                    () -> assertThat(saved.getCompletionType())
                            .isEqualTo(ChatMessageOutboxCompletionType.BROKER_DISPATCHED),
                    () -> assertThat(saved.getProcessingToken()).isNull()
            );
        }

        @Test
        void 방송에_실패하면_다음_시도를_예약한다() {
            // given
            ClaimedChatMessageOutbox claimed = outboxTransactionService.claim(outbox.getId(), now)
                    .orElseThrow();

            // when
            boolean updated = outboxTransactionService.recordFailure(
                    outbox.getId(),
                    claimed.processingToken(),
                    "broadcast failed",
                    now.plusSeconds(1)
            );

            // then
            ChatMessageOutbox saved = outboxRepository.findById(outbox.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(updated).isTrue(),
                    () -> assertThat(saved.getStatus()).isEqualTo(ChatMessageOutboxStatus.PENDING),
                    () -> assertThat(saved.getLastError()).isEqualTo("broadcast failed"),
                    () -> assertThat(saved.getNextAttemptAt()).isAfter(now.plusSeconds(1))
            );
        }

        @Test
        void 실패_메시지가_1000자를_넘으면_잘라서_기록한다() {
            // given
            ClaimedChatMessageOutbox claimed = outboxTransactionService.claim(outbox.getId(), now)
                    .orElseThrow();

            // when
            boolean updated = outboxTransactionService.recordFailure(
                    outbox.getId(),
                    claimed.processingToken(),
                    "a".repeat(1001),
                    now.plusSeconds(1)
            );

            // then
            ChatMessageOutbox saved = outboxRepository.findById(outbox.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(updated).isTrue(),
                    () -> assertThat(saved.getStatus()).isEqualTo(ChatMessageOutboxStatus.PENDING),
                    () -> assertThat(saved.getLastError()).hasSize(1000)
            );
        }

        @Test
        void 다음_처리권을_획득해도_직전_실패_원인을_보존한다() {
            // given
            ClaimedChatMessageOutbox firstClaim = outboxTransactionService.claim(outbox.getId(), now)
                    .orElseThrow();
            outboxTransactionService.recordFailure(
                    outbox.getId(),
                    firstClaim.processingToken(),
                    "first failure",
                    now
            );
            ZonedDateTime nextAttemptAt = outboxRepository.findById(outbox.getId()).orElseThrow().getNextAttemptAt();

            // when
            outboxTransactionService.claim(outbox.getId(), nextAttemptAt).orElseThrow();

            // then
            assertThat(outboxRepository.findById(outbox.getId()).orElseThrow().getLastError())
                    .isEqualTo("first failure");
        }

        @Test
        void 최대_시도_횟수에_도달하면_FAILED로_종료한다() {
            // given
            ZonedDateTime attemptTime = now;
            for (int attempt = 1; attempt <= 3; attempt++) {
                ClaimedChatMessageOutbox claimed = outboxTransactionService.claim(outbox.getId(), attemptTime)
                        .orElseThrow();
                outboxTransactionService.recordFailure(
                        outbox.getId(),
                        claimed.processingToken(),
                        "failure-" + attempt,
                        attemptTime
                );
                attemptTime = outboxRepository.findById(outbox.getId()).orElseThrow().getNextAttemptAt();
            }

            // when
            ChatMessageOutbox saved = outboxRepository.findById(outbox.getId()).orElseThrow();

            // then
            assertAll(
                    () -> assertThat(saved.getStatus()).isEqualTo(ChatMessageOutboxStatus.FAILED),
                    () -> assertThat(saved.getAttemptCount()).isEqualTo(3),
                    () -> assertThat(saved.getLastError()).isEqualTo("failure-3")
            );
        }
    }

    @Nested
    class 오래된_처리를_복구한다 {

        @Test
        void 최대_시도_횟수를_사용한_처리가_만료되면_FAILED로_종료한다() {
            // given
            ZonedDateTime attemptTime = now.minusMinutes(10);
            for (int attempt = 1; attempt < 3; attempt++) {
                ClaimedChatMessageOutbox claimed = outboxTransactionService.claim(outbox.getId(), attemptTime)
                        .orElseThrow();
                outboxTransactionService.recordFailure(
                        outbox.getId(),
                        claimed.processingToken(),
                        "failure-" + attempt,
                        attemptTime
                );
                attemptTime = outboxRepository.findById(outbox.getId()).orElseThrow().getNextAttemptAt();
            }
            outboxTransactionService.claim(outbox.getId(), attemptTime).orElseThrow();

            // when
            int recoveredCount = outboxTransactionService.recoverStaleProcessing(
                    attemptTime.plusMinutes(1),
                    attemptTime.plusMinutes(2)
            );

            // then
            ChatMessageOutbox saved = outboxRepository.findById(outbox.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(recoveredCount).isEqualTo(1),
                    () -> assertThat(saved.getStatus()).isEqualTo(ChatMessageOutboxStatus.FAILED),
                    () -> assertThat(saved.getAttemptCount()).isEqualTo(3)
            );
        }

        @Test
        void 회수된_이전_worker의_늦은_결과가_새로운_완료_상태를_덮지_못한다() {
            // given
            ZonedDateTime firstAttemptAt = now.minusMinutes(2);
            ClaimedChatMessageOutbox firstClaim = outboxTransactionService.claim(outbox.getId(), firstAttemptAt)
                    .orElseThrow();
            outboxTransactionService.recoverStaleProcessing(now.minusMinutes(1), now);
            ClaimedChatMessageOutbox secondClaim = outboxTransactionService.claim(outbox.getId(), now)
                    .orElseThrow();
            outboxTransactionService.markCompleted(
                    outbox.getId(),
                    secondClaim.processingToken(),
                    ChatMessageOutboxCompletionType.BROKER_DISPATCHED,
                    now.plusSeconds(1)
            );

            // when
            boolean staleUpdate = outboxTransactionService.recordFailure(
                    outbox.getId(),
                    firstClaim.processingToken(),
                    "late failure",
                    now.plusSeconds(2)
            );

            // then
            ChatMessageOutbox saved = outboxRepository.findById(outbox.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(staleUpdate).isFalse(),
                    () -> assertThat(saved.getStatus()).isEqualTo(ChatMessageOutboxStatus.COMPLETED),
                    () -> assertThat(saved.getCompletionType())
                            .isEqualTo(ChatMessageOutboxCompletionType.BROKER_DISPATCHED)
            );
        }

        @Test
        void 회수된_이전_worker는_재획득된_Outbox를_완료할_수_없다() {
            // given
            ZonedDateTime firstAttemptAt = now.minusMinutes(2);
            ClaimedChatMessageOutbox firstClaim = outboxTransactionService.claim(outbox.getId(), firstAttemptAt)
                    .orElseThrow();
            outboxTransactionService.recoverStaleProcessing(now.minusMinutes(1), now);
            ClaimedChatMessageOutbox secondClaim = outboxTransactionService.claim(outbox.getId(), now)
                    .orElseThrow();

            // when
            boolean staleUpdate = outboxTransactionService.markCompleted(
                    outbox.getId(),
                    firstClaim.processingToken(),
                    ChatMessageOutboxCompletionType.BROKER_DISPATCHED,
                    now.plusSeconds(1)
            );

            // then
            ChatMessageOutbox saved = outboxRepository.findById(outbox.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(staleUpdate).isFalse(),
                    () -> assertThat(saved.getStatus()).isEqualTo(ChatMessageOutboxStatus.PROCESSING),
                    () -> assertThat(saved.getProcessingToken()).isEqualTo(secondClaim.processingToken()),
                    () -> assertThat(saved.getCompletionType()).isNull()
            );
        }
    }

    @Nested
    class 내역_조회로_완료한다 {

        @Test
        void 방송을_한_번도_시도하지_않은_메시지는_내역_조회로_완료하지_않는다() {
            // when
            int completedCount = outboxTransactionService.markHistoryServed(
                    recipient.getId(),
                    List.of(outbox.getChatMessage().getId()),
                    now
            );

            // then
            assertAll(
                    () -> assertThat(completedCount).isZero(),
                    () -> assertThat(outboxRepository.findById(outbox.getId()).orElseThrow().getStatus())
                            .isEqualTo(ChatMessageOutboxStatus.PENDING)
            );
        }

        @Test
        void 처리_중인_메시지는_내역_조회로_완료하지_않아_송신자_방송을_보존한다() {
            // given
            ClaimedChatMessageOutbox claimed = outboxTransactionService.claim(outbox.getId(), now)
                    .orElseThrow();

            // when
            int completedCount = outboxTransactionService.markHistoryServed(
                    recipient.getId(),
                    List.of(outbox.getChatMessage().getId()),
                    now.plusSeconds(1)
            );
            boolean workerCompleted = outboxTransactionService.markCompleted(
                    outbox.getId(),
                    claimed.processingToken(),
                    ChatMessageOutboxCompletionType.BROKER_DISPATCHED,
                    now.plusSeconds(2)
            );

            // then
            ChatMessageOutbox saved = outboxRepository.findById(outbox.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(completedCount).isZero(),
                    () -> assertThat(workerCompleted).isTrue(),
                    () -> assertThat(saved.getStatus()).isEqualTo(ChatMessageOutboxStatus.COMPLETED),
                    () -> assertThat(saved.getCompletionType())
                            .isEqualTo(ChatMessageOutboxCompletionType.BROKER_DISPATCHED)
            );
        }

        @Test
        void 재시도_상한에_도달한_메시지는_HISTORY_SERVED로_완료한다() {
            // given
            ZonedDateTime attemptTime = now;
            for (int attempt = 1; attempt <= 3; attempt++) {
                ClaimedChatMessageOutbox claimed = outboxTransactionService.claim(outbox.getId(), attemptTime)
                        .orElseThrow();
                outboxTransactionService.recordFailure(
                        outbox.getId(),
                        claimed.processingToken(),
                        "failure-" + attempt,
                        attemptTime
                );
                attemptTime = outboxRepository.findById(outbox.getId()).orElseThrow().getNextAttemptAt();
            }

            // when
            int completedCount = outboxTransactionService.markHistoryServed(
                    recipient.getId(),
                    List.of(outbox.getChatMessage().getId()),
                    now.plusMinutes(1)
            );

            // then
            ChatMessageOutbox saved = outboxRepository.findById(outbox.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(completedCount).isEqualTo(1),
                    () -> assertThat(saved.getStatus()).isEqualTo(ChatMessageOutboxStatus.COMPLETED),
                    () -> assertThat(saved.getCompletionType())
                            .isEqualTo(ChatMessageOutboxCompletionType.HISTORY_SERVED)
            );
        }

        @Test
        void 다른_참여자의_조회는_완료_처리하지_않는다() {
            // given
            ChatParticipant other = chatParticipantFixture.참여자(
                    siteUserFixture.사용자(3, "other").getId(),
                    chatRoomFixture.채팅방(false)
            );

            // when
            int completedCount = outboxTransactionService.markHistoryServed(
                    other.getId(),
                    List.of(outbox.getChatMessage().getId()),
                    now
            );

            // then
            assertAll(
                    () -> assertThat(completedCount).isZero(),
                    () -> assertThat(outboxRepository.findById(outbox.getId()).orElseThrow().getStatus())
                            .isEqualTo(ChatMessageOutboxStatus.PENDING)
            );
        }
    }
}
