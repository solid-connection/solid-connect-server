package com.example.solidconnection.common.validation.validator;

import com.example.solidconnection.admin.dto.GpaScoreUpdateRequest;
import com.example.solidconnection.admin.dto.LanguageTestScoreUpdateRequest;
import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.application.domain.VerifyStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.example.solidconnection.common.exception.ErrorCode.REJECTED_REASON_REQUIRED;
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

    @Nested
    class GPA_점수_거절_사유_검증 {

        @Test
        void 거절_상태일_때_거절사유가_있으면_유효하다() {
            // given
            GpaScoreUpdateRequest request = new GpaScoreUpdateRequest(
                    3.0,
                    4.5,
                    VerifyStatus.REJECTED,
                    "부적합"
            );

            // when
            Set<ConstraintViolation<GpaScoreUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        void 거절_상태일_때_거절사유가_없으면_예외_응답을_반환한다() {
            // given
            GpaScoreUpdateRequest request = new GpaScoreUpdateRequest(
                    3.0,
                    4.5,
                    VerifyStatus.REJECTED,
                    null
            );

            // when
            Set<ConstraintViolation<GpaScoreUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations)
                    .extracting(MESSAGE)
                    .contains(REJECTED_REASON_REQUIRED.getMessage());
        }
    }

    @Nested
    class 어학_점수_거절_사유_검증 {

        @Test
        void 거절_상태일_때_거절사유가_있으면_유효하다() {
            // given
            LanguageTestScoreUpdateRequest request = new LanguageTestScoreUpdateRequest(
                    LanguageTestType.TOEIC,
                    "900",
                    VerifyStatus.REJECTED,
                    "부적합"
            );

            // when
            Set<ConstraintViolation<LanguageTestScoreUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        void 거절_상태일_때_거절사유가_없으면_예외_응답을_반환한다() {
            // given
            LanguageTestScoreUpdateRequest request = new LanguageTestScoreUpdateRequest(
                    LanguageTestType.TOEIC,
                    "900",
                    VerifyStatus.REJECTED,
                    null
            );

            // when
            Set<ConstraintViolation<LanguageTestScoreUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations)
                    .extracting(MESSAGE)
                    .contains(REJECTED_REASON_REQUIRED.getMessage());
        }
    }
}
