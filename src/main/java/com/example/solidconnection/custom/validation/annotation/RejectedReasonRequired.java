package com.example.solidconnection.custom.validation.annotation;

import com.example.solidconnection.custom.validation.validator.RejectedReasonValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RejectedReasonValidator.class)
public @interface RejectedReasonRequired {

    String message() default "거절 사유 입력값이 올바르지 않습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
