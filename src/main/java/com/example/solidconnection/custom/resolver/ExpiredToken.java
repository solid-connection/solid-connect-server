package com.example.solidconnection.custom.resolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// todo: 사용되지 않음, 다른 PR에서 삭제하고 더 효율적인 구조를 고민해봐야 함
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpiredToken {
}
