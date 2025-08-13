package com.example.solidconnection.siteuser.dto.validation;

import static com.example.solidconnection.common.exception.ErrorCode.PASSWORD_NOT_CHANGED;
import static com.example.solidconnection.common.exception.ErrorCode.PASSWORD_NOT_CONFIRMED;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.solidconnection.siteuser.dto.PasswordUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("비밀번호 변경 유효성 검사 테스트")
class PasswordConfirmationValidatorTest {

    private static final String MESSAGE = "message";

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void 유효한_비밀번호_변경_요청은_검증을_통과한다() {
        // given
        PasswordUpdateRequest request = new PasswordUpdateRequest("currentPassword123", "newPassword123!", "newPassword123!");

        // when
        Set<ConstraintViolation<PasswordUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Nested
    class 유효하지_않은_비밀번호_변경_테스트 {

        @Test
        void 새로운_비밀번호와_확인_비밀번호가_일치하지_않으면_검증에_실패한다() {
            // given
            PasswordUpdateRequest request = new PasswordUpdateRequest("currentPassword123", "newPassword123!", "differentPassword123!");

            // when
            Set<ConstraintViolation<PasswordUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations)
                    .isNotEmpty()
                    .extracting(MESSAGE)
                    .contains(PASSWORD_NOT_CONFIRMED.getMessage());
        }

        @Test
        void 현재_비밀번호와_새로운_비밀번호가_같으면_검증에_실패한다() {
            // given
            PasswordUpdateRequest request = new PasswordUpdateRequest("currentPassword123", "currentPassword123", "currentPassword123");

            // when
            Set<ConstraintViolation<PasswordUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations)
                    .isNotEmpty()
                    .extracting(MESSAGE)
                    .contains(PASSWORD_NOT_CHANGED.getMessage());
        }
    }
}
