package com.example.solidconnection.chat.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
    }
}
