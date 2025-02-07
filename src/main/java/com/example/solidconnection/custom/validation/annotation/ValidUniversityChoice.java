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

    String message() default "2지망 없이 3지망을 선택할 수 없습니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
