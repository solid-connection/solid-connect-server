package com.example.solidconnection.siteuser.dto.validation;

import static com.example.solidconnection.common.exception.ErrorCode.PASSWORD_NOT_CHANGED;
import static com.example.solidconnection.common.exception.ErrorCode.PASSWORD_NOT_CONFIRMED;

import com.example.solidconnection.siteuser.dto.PasswordUpdateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

public class PasswordConfirmationValidator implements ConstraintValidator<PasswordConfirmation, PasswordUpdateRequest> {

    @Override
    public boolean isValid(PasswordUpdateRequest request, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        if (isNewPasswordNotConfirmed(request)) {
            addConstraintViolation(context, PASSWORD_NOT_CONFIRMED.getMessage(), "newPasswordConfirmation");

            return false;
        }

        if (isPasswordUnchanged(request)) {
            addConstraintViolation(context, PASSWORD_NOT_CHANGED.getMessage(), "newPassword");

            return false;
        }

        return true;
    }

    private boolean isNewPasswordNotConfirmed(PasswordUpdateRequest request) {
        return !Objects.equals(request.newPassword(), request.newPasswordConfirmation());
    }

    private boolean isPasswordUnchanged(PasswordUpdateRequest request) {
        return Objects.equals(request.currentPassword(), request.newPassword());
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message, String propertyName) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(propertyName)
                .addConstraintViolation();
    }
}
