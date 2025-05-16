package com.example.solidconnection.security.aspect;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.security.annotation.RequireAdminAccess;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import static com.example.solidconnection.common.exception.ErrorCode.ACCESS_DENIED;
import static com.example.solidconnection.siteuser.domain.Role.ADMIN;

@Aspect
@Component
@RequiredArgsConstructor
public class AdminAuthorizationAspect {

    @Around("@annotation(requireAdminAccess)")
    public Object checkAdminAccess(ProceedingJoinPoint joinPoint,
                                   RequireAdminAccess requireAdminAccess) throws Throwable {
        SiteUser siteUser = null;
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof SiteUser) {
                siteUser = (SiteUser) arg;
                break;
            }
        }
        if (siteUser == null || !ADMIN.equals(siteUser.getRole())) {
            throw new CustomException(ACCESS_DENIED);
        }
        return joinPoint.proceed();
    }
}
