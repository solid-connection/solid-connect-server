package com.example.solidconnection.siteuser.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("학교 이메일 인증 요청 유효성 검사 테스트")
class SchoolEmailRequestTest {

    private static final String MESSAGE = "message";
    private static final String SCHOOL_EMAIL_MAX_LENGTH_MESSAGE = "학교 이메일은 100자 이하여야 합니다";

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    class 학교_이메일_길이_검증 {

        @Test
        void 학교_이메일이_100자이면_검증을_통과한다() {
            // given
            SchoolEmailRequest request = new SchoolEmailRequest(emailWithLength(100));

            // when
            Set<ConstraintViolation<SchoolEmailRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        void 학교_이메일이_100자를_초과하면_검증에_실패한다() {
            // given
            SchoolEmailRequest request = new SchoolEmailRequest(emailWithLength(101));

            // when
            Set<ConstraintViolation<SchoolEmailRequest>> violations = validator.validate(request);

            // then
            assertThat(violations)
                    .extracting(MESSAGE)
                    .contains(SCHOOL_EMAIL_MAX_LENGTH_MESSAGE);
        }
    }

    private String emailWithLength(int length) {
        String prefix = "a@";
        String domainPrefix = "b".repeat(63) + ".";
        String suffix = ".edu";
        String domain = domainPrefix + "c".repeat(length - prefix.length() - domainPrefix.length() - suffix.length());
        return prefix + domain + suffix;
    }
}
