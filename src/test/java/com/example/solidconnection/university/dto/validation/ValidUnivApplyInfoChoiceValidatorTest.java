package com.example.solidconnection.university.dto.validation;

import static com.example.solidconnection.common.exception.ErrorCode.DUPLICATE_UNIV_APPLY_INFO_CHOICE;
import static com.example.solidconnection.common.exception.ErrorCode.FIRST_CHOICE_REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.solidconnection.application.dto.UnivApplyInfoChoiceRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("대학 선택 유효성 검사 테스트")
class ValidUnivApplyInfoChoiceValidatorTest {

    private static final String MESSAGE = "message";

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void 정상적인_지망_선택은_유효하다() {
        // given
        UnivApplyInfoChoiceRequest request = new UnivApplyInfoChoiceRequest(List.of(1L, 2L, 3L));

        // when
        Set<ConstraintViolation<UnivApplyInfoChoiceRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    void 첫_번째_지망만_선택하는_것은_유효하다() {
        // given
        UnivApplyInfoChoiceRequest request = new UnivApplyInfoChoiceRequest(List.of(1L));

        // when
        Set<ConstraintViolation<UnivApplyInfoChoiceRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    void 지망을_선택하지_않으면_예외가_발생한다() {
        // given
        UnivApplyInfoChoiceRequest request = new UnivApplyInfoChoiceRequest(List.of());

        // when
        Set<ConstraintViolation<UnivApplyInfoChoiceRequest>> violations = validator.validate(request);

        // then
        assertThat(violations)
                .isNotEmpty()
                .extracting(MESSAGE)
                .contains(FIRST_CHOICE_REQUIRED.getMessage());
    }

    @Test
    void 대학을_중복_선택하면_예외가_발생한다() {
        // given
        UnivApplyInfoChoiceRequest request = new UnivApplyInfoChoiceRequest(List.of(1L, 1L, 2L));

        // when
        Set<ConstraintViolation<UnivApplyInfoChoiceRequest>> violations = validator.validate(request);

        // then
        assertThat(violations)
                .isNotEmpty()
                .extracting(MESSAGE)
                .contains(DUPLICATE_UNIV_APPLY_INFO_CHOICE.getMessage());
    }
}
