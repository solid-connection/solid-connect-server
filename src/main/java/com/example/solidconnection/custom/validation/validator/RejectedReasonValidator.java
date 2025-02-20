package com.example.solidconnection.custom.validation.validator;

import com.example.solidconnection.admin.dto.GpaScoreUpdateRequest;
import com.example.solidconnection.custom.validation.annotation.RejectedReasonRequired;
import com.example.solidconnection.type.VerifyStatus;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static com.example.solidconnection.custom.exception.ErrorCode.REJECTED_REASON_REQUIRED;

public class RejectedReasonValidator implements ConstraintValidator<RejectedReasonRequired, GpaScoreUpdateRequest> {

    private static final String REJECTED_REASON = "rejectedReason";

    @Override
    public boolean isValid(GpaScoreUpdateRequest request, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        if (isRejectedWithoutReason(request)) {
            addValidationError(context, REJECTED_REASON_REQUIRED.getMessage());
            return false;
        }
        return true;
    }

    private boolean isRejectedWithoutReason(GpaScoreUpdateRequest request) {
        return request.verifyStatus().equals(VerifyStatus.REJECTED)
                && StringUtils.isBlank(request.rejectedReason());
    }

    private void addValidationError(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(REJECTED_REASON)
                .addConstraintViolation();
    }
}
