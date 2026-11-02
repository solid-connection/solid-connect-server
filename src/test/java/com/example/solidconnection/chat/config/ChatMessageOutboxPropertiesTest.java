package com.example.solidconnection.chat.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;

@DisplayName("채팅 메시지 Outbox 설정 테스트")
class ChatMessageOutboxPropertiesTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Nested
    class 양수_설정을_검증한다 {

        @Test
        void 최대_시도_횟수가_0이면_검증에_실패한다() {
            // given
            ChatMessageOutboxProperties properties = new ChatMessageOutboxProperties();
            properties.setMaxAttempts(0);

            // when & then
            assertThat(validator.validate(properties))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactly("maxAttempts");
        }

        @Test
        void 배치_크기가_0이면_검증에_실패한다() {
            // given
            ChatMessageOutboxProperties properties = new ChatMessageOutboxProperties();
            properties.setBatchSize(0);

            // when & then
            assertThat(validator.validate(properties))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactly("batchSize");
        }

        @Test
        void 초기_재시도_지연이_0이면_검증에_실패한다() {
            // given
            ChatMessageOutboxProperties properties = new ChatMessageOutboxProperties();
            properties.setInitialRetryDelay(Duration.ZERO);

            // when & then
            assertThat(validator.validate(properties))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactly("initialRetryDelay");
        }

        @Test
        void 최대_재시도_지연이_0이면_검증에_실패한다() {
            // given
            ChatMessageOutboxProperties properties = new ChatMessageOutboxProperties();
            properties.setMaxRetryDelay(Duration.ZERO);

            // when & then
            assertThat(validator.validate(properties))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactly("maxRetryDelay");
        }

        @Test
        void 처리_타임아웃이_0이면_검증에_실패한다() {
            // given
            ChatMessageOutboxProperties properties = new ChatMessageOutboxProperties();
            properties.setProcessingTimeout(Duration.ZERO);

            // when & then
            assertThat(validator.validate(properties))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactly("processingTimeout");
        }

        @Test
        void Duration_설정이_null이면_검증에_실패한다() {
            // given
            ChatMessageOutboxProperties properties = new ChatMessageOutboxProperties();
            properties.setInitialRetryDelay(null);
            properties.setMaxRetryDelay(null);
            properties.setProcessingTimeout(null);

            // when & then
            assertThat(validator.validate(properties))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactlyInAnyOrder("initialRetryDelay", "maxRetryDelay", "processingTimeout");
        }
    }

    @Nested
    class 배포_기본값을_검증한다 {

        @Test
        void 클라이언트_멱등_처리_전에는_재시도_스케줄러를_비활성화한다() throws IOException {
            // given
            YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

            // when
            Object enabled = loader.load(
                            "application-variable",
                            new ClassPathResource("config/application-variable.yml")
                    ).stream()
                    .map(source -> source.getProperty("chat.outbox.scheduling.enabled"))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseThrow();

            // then
            assertThat(enabled).isEqualTo(false);
        }
    }
}
