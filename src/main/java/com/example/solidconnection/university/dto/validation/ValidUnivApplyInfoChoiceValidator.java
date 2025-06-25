package com.example.solidconnection.university.dto.validation;

import com.example.solidconnection.application.dto.UnivApplyInfoChoiceRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.example.solidconnection.common.exception.ErrorCode.DUPLICATE_UNIV_APPLY_INFO_CHOICE;
import static com.example.solidconnection.common.exception.ErrorCode.FIRST_CHOICE_REQUIRED;
import static com.example.solidconnection.common.exception.ErrorCode.THIRD_CHOICE_REQUIRES_SECOND;

public class ValidUnivApplyInfoChoiceValidator implements ConstraintValidator<ValidUnivApplyInfoChoice, UnivApplyInfoChoiceRequest> {

    @Override
    public boolean isValid(UnivApplyInfoChoiceRequest request, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        if (isFirstChoiceNotSelected(request)) {
            context.buildConstraintViolationWithTemplate(FIRST_CHOICE_REQUIRED.getMessage())
                    .addConstraintViolation();
            return false;
        }

        if (isThirdChoiceWithoutSecond(request)) {
            context.buildConstraintViolationWithTemplate(THIRD_CHOICE_REQUIRES_SECOND.getMessage())
                    .addConstraintViolation();
            return false;
        }

        if (isDuplicate(request)) {
            context.buildConstraintViolationWithTemplate(DUPLICATE_UNIV_APPLY_INFO_CHOICE.getMessage())
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean isFirstChoiceNotSelected(UnivApplyInfoChoiceRequest request) {
        return request.firstChoiceUniversityId() == null;
    }

    private boolean isThirdChoiceWithoutSecond(UnivApplyInfoChoiceRequest request) {
        return request.thirdChoiceUniversityId() != null && request.secondChoiceUniversityId() == null;
    }

    private boolean isDuplicate(UnivApplyInfoChoiceRequest request) {
        Set<Long> uniqueIds = new HashSet<>();
        return Stream.of(
                        request.firstChoiceUniversityId(),
                        request.secondChoiceUniversityId(),
                        request.thirdChoiceUniversityId()
                )
                .filter(Objects::nonNull)
                .anyMatch(id -> !uniqueIds.add(id));
    }
}
