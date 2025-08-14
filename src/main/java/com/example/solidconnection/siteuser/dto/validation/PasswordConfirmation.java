package com.example.solidconnection.siteuser.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordConfirmationValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordConfirmation {

    String message() default "비밀번호 변경 과정에서 오류가 발생했습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
