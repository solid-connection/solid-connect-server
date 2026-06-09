package com.example.solidconnection.university.dto.validation;

import static com.example.solidconnection.common.exception.ErrorCode.DUPLICATE_UNIV_APPLY_INFO_CHOICE;
import static com.example.solidconnection.common.exception.ErrorCode.FIRST_CHOICE_REQUIRED;

import com.example.solidconnection.application.dto.UnivApplyInfoChoiceRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ValidUnivApplyInfoChoiceValidator
        implements ConstraintValidator<ValidUnivApplyInfoChoice, UnivApplyInfoChoiceRequest> {

    @Override
    public boolean isValid(UnivApplyInfoChoiceRequest request, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        List<Long> ids = request.univApplyInfoIds();

        if (ids == null || ids.isEmpty()) {
            context.buildConstraintViolationWithTemplate(FIRST_CHOICE_REQUIRED.getMessage())
                    .addConstraintViolation();
            return false;
        }

        if (hasDuplicate(ids)) {
            context.buildConstraintViolationWithTemplate(DUPLICATE_UNIV_APPLY_INFO_CHOICE.getMessage())
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean hasDuplicate(List<Long> ids) {
        Set<Long> unique = new HashSet<>();
        return ids.stream().anyMatch(id -> !unique.add(id));
    }
}
