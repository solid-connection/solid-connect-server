package com.example.solidconnection.custom.validation.validator;

import com.example.solidconnection.application.dto.UniversityChoiceRequest;
import com.example.solidconnection.custom.validation.annotation.ValidUniversityChoice;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;

import static com.example.solidconnection.custom.exception.ErrorCode.DUPLICATE_UNIVERSITY_CHOICE;
import static com.example.solidconnection.custom.exception.ErrorCode.THIRD_CHOICE_REQUIRES_SECOND;

public class ValidUniversityChoiceValidator implements ConstraintValidator<ValidUniversityChoice, UniversityChoiceRequest> {

    @Override
    public boolean isValid(UniversityChoiceRequest request, ConstraintValidatorContext context) {
        if (request.thirdChoiceUniversityId() != null && request.secondChoiceUniversityId() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(THIRD_CHOICE_REQUIRES_SECOND.getMessage())
                    .addConstraintViolation();
            return false;
        }

        Set<Long> uniqueUniversityIds = new HashSet<>();
        uniqueUniversityIds.add(request.firstChoiceUniversityId());

        return isValidChoice(request.secondChoiceUniversityId(), uniqueUniversityIds, context) &&
                isValidChoice(request.thirdChoiceUniversityId(), uniqueUniversityIds, context);
    }

    private boolean isValidChoice(Long choiceId, Set<Long> uniqueIds, ConstraintValidatorContext context) {
        if (choiceId == null)
            return true;

        if (!uniqueIds.add(choiceId)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(DUPLICATE_UNIVERSITY_CHOICE.getMessage())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
