package com.example.solidconnection.custom.validation.validator;

import com.example.solidconnection.application.dto.UniversityChoiceRequest;
import com.example.solidconnection.custom.validation.annotation.ValidUniversityChoice;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;

public class ValidUniversityChoiceValidator implements ConstraintValidator<ValidUniversityChoice, UniversityChoiceRequest> {

    public static final String ERROR_THIRD_CHOICE_WITHOUT_SECOND = "2지망 없이 3지망을 선택할 수 없습니다.";
    public static final String ERROR_DUPLICATE_CHOICE = "지망 선택이 중복되었습니다";

    @Override
    public boolean isValid(UniversityChoiceRequest request, ConstraintValidatorContext context) {
        if (request.thirdChoiceUniversityId() != null && request.secondChoiceUniversityId() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ERROR_THIRD_CHOICE_WITHOUT_SECOND)
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
            context.buildConstraintViolationWithTemplate(ERROR_DUPLICATE_CHOICE)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
