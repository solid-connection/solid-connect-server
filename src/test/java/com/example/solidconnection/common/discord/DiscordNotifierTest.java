package com.example.solidconnection.common.discord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

import com.example.solidconnection.common.discord.domain.DiscordNotification;
import com.example.solidconnection.common.discord.service.DiscordNotificationService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiscordNotifier 테스트")
class DiscordNotifierTest {

    private static final String WEBHOOK_URL = "https://discord.test/webhooks/channel";
    private static final String MESSAGE_ID = "message-id";
    private static final long REVIEW_ID = 1L;
    private static final DiscordNotificationType REVIEW_TYPE = DiscordNotificationType.GPA_SCORE;

    @Mock
    private DiscordWebhookSender discordWebhookSender;

    @Mock
    private DiscordNotificationService discordNotificationService;

    private DiscordNotifier discordNotifier;

    @BeforeEach
    void setUp() {
        discordNotifier = notifierWith(WEBHOOK_URL, "dev");
    }

    private DiscordNotifier notifierWith(String webhookUrl, String environment) {
        return new DiscordNotifier(discordWebhookSender, discordNotificationService, webhookUrl, environment);
    }

    private void 알림이_저장되어_있다() {
        DiscordNotification notification = DiscordNotification.of(REVIEW_TYPE, REVIEW_ID, "channel-id", MESSAGE_ID);
        given(discordNotificationService.findByReviewTypeAndReviewId(REVIEW_TYPE, REVIEW_ID))
                .willReturn(Optional.of(notification));
    }

    @Nested
    @DisplayName("검수 결과 표시")
    class 검수_결과를_표시한다 {

        @Test
        void 마커를_붙인_본문으로_원본_메시지를_한_번만_편집한다() {
            // given
            알림이_저장되어_있다();
            ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);

            // when
            discordNotifier.markReviewResult(REVIEW_TYPE, REVIEW_ID, "홍길동", DiscordReviewMarker.APPROVED.getValue());

            // then
            then(discordWebhookSender).should()
                    .editMessage(eq(WEBHOOK_URL), eq(MESSAGE_ID), contentCaptor.capture());
            String content = contentCaptor.getValue();
            assertThat(content).startsWith("(승인되었습니다.) ").contains("홍길동").contains("[개발 서버 알림입니다]");
        }

        @Test
        void 재검수하면_마커가_누적되지_않고_최종_결과만_남는다() {
            // given
            알림이_저장되어_있다();
            ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);

            // when
            discordNotifier.markReviewResult(REVIEW_TYPE, REVIEW_ID, "홍길동", DiscordReviewMarker.REJECTED.getValue());

            // then
            then(discordWebhookSender).should()
                    .editMessage(anyString(), anyString(), contentCaptor.capture());
            String content = contentCaptor.getValue();
            assertThat(content).startsWith("(반려되었습니다.) ");
            assertThat(content).doesNotContain("승인되었습니다");
        }
    }

    @Nested
    @DisplayName("전송하지 않는 경우")
    class 전송하지_않는다 {

        @Test
        void webhook_url_이_없으면_조회조차_하지_않는다() {
            // given
            discordNotifier = notifierWith("", "dev");

            // when
            discordNotifier.markReviewResult(REVIEW_TYPE, REVIEW_ID, "홍길동", DiscordReviewMarker.APPROVED.getValue());

            // then
            then(discordNotificationService).should(never()).findByReviewTypeAndReviewId(any(), anyLong());
            then(discordWebhookSender).should(never()).editMessage(anyString(), anyString(), anyString());
        }

        @Test
        void local_환경이면_조회조차_하지_않는다() {
            // given
            discordNotifier = notifierWith(WEBHOOK_URL, "local");

            // when
            discordNotifier.markReviewResult(REVIEW_TYPE, REVIEW_ID, "홍길동", DiscordReviewMarker.APPROVED.getValue());

            // then
            then(discordNotificationService).should(never()).findByReviewTypeAndReviewId(any(), anyLong());
            then(discordWebhookSender).should(never()).editMessage(anyString(), anyString(), anyString());
        }

        @Test
        void 저장된_알림이_없으면_편집하지_않는다() {
            // given
            given(discordNotificationService.findByReviewTypeAndReviewId(REVIEW_TYPE, REVIEW_ID))
                    .willReturn(Optional.empty());

            // when
            discordNotifier.markReviewResult(REVIEW_TYPE, REVIEW_ID, "홍길동", DiscordReviewMarker.APPROVED.getValue());

            // then
            then(discordWebhookSender).should(never()).editMessage(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("편집 실패 처리")
    class 편집_실패를_처리한다 {

        @Test
        void 편집_대상이_없으면_검수를_실패시키지_않는다() {
            // given
            알림이_저장되어_있다();
            HttpClientErrorException notFound = HttpClientErrorException.create(
                    HttpStatus.NOT_FOUND, "Not Found", null, null, null);
            willThrow(notFound)
                    .given(discordWebhookSender).editMessage(anyString(), anyString(), anyString());

            // when & then
            assertThatCode(() -> discordNotifier.markReviewResult(
                    REVIEW_TYPE, REVIEW_ID, "홍길동", DiscordReviewMarker.APPROVED.getValue()))
                    .doesNotThrowAnyException();
        }

        @Test
        void 디스코드_장애는_그대로_전파한다() {
            // given
            알림이_저장되어_있다();
            HttpServerErrorException serverError = HttpServerErrorException.create(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", null, null, null);
            willThrow(serverError)
                    .given(discordWebhookSender).editMessage(anyString(), anyString(), anyString());

            // when & then
            assertThatThrownBy(() -> discordNotifier.markReviewResult(
                    REVIEW_TYPE, REVIEW_ID, "홍길동", DiscordReviewMarker.APPROVED.getValue()))
                    .isInstanceOf(HttpServerErrorException.class);
        }
    }
}
