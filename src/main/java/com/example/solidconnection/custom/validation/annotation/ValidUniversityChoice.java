package com.example.solidconnection.custom.validation.annotation;

import com.example.solidconnection.custom.validation.validator.ValidUniversityChoiceValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidUniversityChoiceValidator.class)
public @interface ValidUniversityChoice {

    String message() default "유효하지 않은 지망 대학 선택입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
