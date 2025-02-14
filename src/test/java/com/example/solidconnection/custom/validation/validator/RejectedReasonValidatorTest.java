package com.example.solidconnection.custom.validation.validator;

import com.example.solidconnection.admin.dto.GpaScoreVerifyRequest;
import com.example.solidconnection.type.VerifyStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.example.solidconnection.custom.exception.ErrorCode.REJECTED_REASON_NOT_ALLOWED;
import static com.example.solidconnection.custom.exception.ErrorCode.REJECTED_REASON_REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("거절 사유 유효성 검사 테스트")
class RejectedReasonValidatorTest {

    private static final String MESSAGE = "message";

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void 거절상태일때_거절사유가_있으면_유효하다() {
        // given
        GpaScoreVerifyRequest request = new GpaScoreVerifyRequest(VerifyStatus.REJECTED, "부적합");

        // when
        Set<ConstraintViolation<GpaScoreVerifyRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    void 승인상태일때_거절사유가_없으면_유효하다() {
        // given
        GpaScoreVerifyRequest request = new GpaScoreVerifyRequest(VerifyStatus.APPROVED, null);

        // when
        Set<ConstraintViolation<GpaScoreVerifyRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    void 거절상태일때_거절사유_없으면_예외_응답을_반환한다() {
        // given
        GpaScoreVerifyRequest request = new GpaScoreVerifyRequest(VerifyStatus.REJECTED, null);

        // when
        Set<ConstraintViolation<GpaScoreVerifyRequest>> violations = validator.validate(request);

        // then
        assertThat(violations)
                .extracting(MESSAGE)
                .contains(REJECTED_REASON_REQUIRED.getMessage());
    }

    @Test
    void 거절상태아닐때_거절사유_입력하면_예외_응답을_반환한다() {
        // given
        GpaScoreVerifyRequest request = new GpaScoreVerifyRequest(VerifyStatus.APPROVED, "사유");

        // when
        Set<ConstraintViolation<GpaScoreVerifyRequest>> violations = validator.validate(request);

        // then
        assertThat(violations)
                .extracting(MESSAGE)
                .contains(REJECTED_REASON_NOT_ALLOWED.getMessage());
    }
}
