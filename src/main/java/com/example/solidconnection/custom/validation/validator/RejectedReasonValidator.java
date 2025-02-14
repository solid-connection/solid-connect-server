package com.example.solidconnection.custom.validation.validator;

import com.example.solidconnection.admin.dto.GpaScoreVerifyRequest;
import com.example.solidconnection.custom.validation.annotation.RejectedReasonRequired;
import com.example.solidconnection.type.VerifyStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static com.example.solidconnection.custom.exception.ErrorCode.REJECTED_REASON_NOT_ALLOWED;
import static com.example.solidconnection.custom.exception.ErrorCode.REJECTED_REASON_REQUIRED;

public class RejectedReasonValidator implements ConstraintValidator<RejectedReasonRequired, GpaScoreVerifyRequest> {

    private static final String REJECTED_REASON = "rejectedReason";

    @Override
    public boolean isValid(GpaScoreVerifyRequest request, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        if (isRejectedWithoutReason(request)) {
            addValidationError(context, REJECTED_REASON_REQUIRED.getMessage());
            return false;
        }

        if (hasReasonWhenNotRejected(request)) {
            addValidationError(context, REJECTED_REASON_NOT_ALLOWED.getMessage());
            return false;
        }

        return true;
    }

    private boolean isRejectedWithoutReason(GpaScoreVerifyRequest request) {
        return request.verifyStatus().equals(VerifyStatus.REJECTED)
                && (request.rejectedReason() == null || request.rejectedReason().trim().isEmpty());
    }

    private boolean hasReasonWhenNotRejected(GpaScoreVerifyRequest request) {
        return !request.verifyStatus().equals(VerifyStatus.REJECTED)
                && request.rejectedReason() != null
                && !request.rejectedReason().trim().isEmpty();
    }

    private void addValidationError(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(REJECTED_REASON)
                .addConstraintViolation();
    }
}
