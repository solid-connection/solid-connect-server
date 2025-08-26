package com.example.solidconnection.application.dto.validation;

import static com.example.solidconnection.common.exception.ErrorCode.REJECTED_REASON_REQUIRED;

import com.example.solidconnection.admin.dto.ScoreUpdateRequest;
import com.example.solidconnection.common.VerifyStatus;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RejectedReasonValidator implements ConstraintValidator<RejectedReasonRequired, ScoreUpdateRequest> {

    private static final String REJECTED_REASON = "rejectedReason";

    @Override
    public boolean isValid(ScoreUpdateRequest request, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        if (isRejectedWithoutReason(request)) {
            addValidationError(context, REJECTED_REASON_REQUIRED.getMessage());
            return false;
        }
        return true;
    }

    private boolean isRejectedWithoutReason(ScoreUpdateRequest request) {
        return request.verifyStatus().equals(VerifyStatus.REJECTED)
                && StringUtils.isBlank(request.rejectedReason());
    }

    private void addValidationError(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(REJECTED_REASON)
                .addConstraintViolation();
    }
}
