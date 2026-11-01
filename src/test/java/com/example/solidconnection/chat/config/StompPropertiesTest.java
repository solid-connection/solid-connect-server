package com.example.solidconnection.chat.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("STOMP 설정 테스트")
class StompPropertiesTest {

    @Nested
    @DisplayName("구독 제한시간 검증")
    class 구독_제한시간을_검증한다 {

        @Test
        void 양수이면_설정을_생성한다() {
            // when & then
            assertThatCode(() -> new StompProperties(null, null, Duration.ofSeconds(30)))
                    .doesNotThrowAnyException();
        }

        @Test
        void 값이_없으면_설정_생성을_거부한다() {
            // when & then
            assertThatThrownBy(() -> new StompProperties(null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("websocket.subscription-timeout must be a positive duration");
        }

        @Test
        void 영이면_설정_생성을_거부한다() {
            // when & then
            assertThatThrownBy(() -> new StompProperties(null, null, Duration.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("websocket.subscription-timeout must be a positive duration");
        }

        @Test
        void 음수이면_설정_생성을_거부한다() {
            // when & then
            assertThatThrownBy(() -> new StompProperties(null, null, Duration.ofSeconds(-1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("websocket.subscription-timeout must be a positive duration");
        }
    }
}
