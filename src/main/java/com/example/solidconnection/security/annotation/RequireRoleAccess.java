package com.example.solidconnection.security.annotation;

import com.example.solidconnection.siteuser.domain.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRoleAccess {
    Role[] roles();
}
