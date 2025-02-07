package com.example.solidconnection.custom.validation.validator;

import com.example.solidconnection.application.dto.UniversityChoiceRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.example.solidconnection.custom.validation.validator.ValidUniversityChoiceValidator.ERROR_DUPLICATE_CHOICE;
import static com.example.solidconnection.custom.validation.validator.ValidUniversityChoiceValidator.ERROR_THIRD_CHOICE_WITHOUT_SECOND;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("대학 선택 유효성 검사 테스트")
class ValidUniversityChoiceValidatorTest {

    private static final String MESSAGE = "message";
    private static final String ERROR_FIRST_CHOICE_IS_NULL = "1지망 대학교를 입력해주세요.";

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void 정상적인_지망_선택은_유효하다() {
        // given
        UniversityChoiceRequest request = new UniversityChoiceRequest(1L, 2L, 3L);

        // when
        Set<ConstraintViolation<UniversityChoiceRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    void 첫_번째_지망만_선택하는_것은_유효하다() {
        // given
        UniversityChoiceRequest request = new UniversityChoiceRequest(1L, null, null);

        // when
        Set<ConstraintViolation<UniversityChoiceRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    void 두_번째_지망_없이_세_번째_지망을_선택하면_예외_응답을_반환한다() {
        // given
        UniversityChoiceRequest request = new UniversityChoiceRequest(1L, null, 3L);

        // when
        Set<ConstraintViolation<UniversityChoiceRequest>> violations = validator.validate(request);

        // then
        assertThat(violations)
                .extracting(MESSAGE)
                .contains(ERROR_THIRD_CHOICE_WITHOUT_SECOND);
    }

    @Test
    void 첫_번째_지망을_선택하지_않으면_예외_응답을_반환한다() {
        // given
        UniversityChoiceRequest request = new UniversityChoiceRequest(null, 2L, 3L);

        // when
        Set<ConstraintViolation<UniversityChoiceRequest>> violations = validator.validate(request);

        // then

        assertThat(violations)
                .isNotEmpty()
                .extracting(MESSAGE)
                .contains(ERROR_FIRST_CHOICE_IS_NULL);
    }

    @Test
    void 대학을_중복_선택하면_예외_응답을_반환한다() {
        // given
        UniversityChoiceRequest request = new UniversityChoiceRequest(1L, 1L, 2L);

        // when
        Set<ConstraintViolation<UniversityChoiceRequest>> violations = validator.validate(request);

        // then
        assertThat(violations)
                .isNotEmpty()
                .extracting(MESSAGE)
                .contains(ERROR_DUPLICATE_CHOICE);
    }
}
