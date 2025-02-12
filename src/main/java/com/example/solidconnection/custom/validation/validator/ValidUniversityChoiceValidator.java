package com.example.solidconnection.custom.validation.validator;

import com.example.solidconnection.application.dto.UniversityChoiceRequest;
import com.example.solidconnection.custom.validation.annotation.ValidUniversityChoice;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.example.solidconnection.custom.exception.ErrorCode.DUPLICATE_UNIVERSITY_CHOICE;
import static com.example.solidconnection.custom.exception.ErrorCode.FIRST_CHOICE_REQUIRED;
import static com.example.solidconnection.custom.exception.ErrorCode.THIRD_CHOICE_REQUIRES_SECOND;

public class ValidUniversityChoiceValidator implements ConstraintValidator<ValidUniversityChoice, UniversityChoiceRequest> {

    @Override
    public boolean isValid(UniversityChoiceRequest request, ConstraintValidatorContext context) {
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
            context.buildConstraintViolationWithTemplate(DUPLICATE_UNIVERSITY_CHOICE.getMessage())
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean isFirstChoiceNotSelected(UniversityChoiceRequest request) {
        return request.firstChoiceUniversityId() == null;
    }

    private boolean isThirdChoiceWithoutSecond(UniversityChoiceRequest request) {
        return request.thirdChoiceUniversityId() != null && request.secondChoiceUniversityId() == null;
    }

    private boolean isDuplicate(UniversityChoiceRequest request) {
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
