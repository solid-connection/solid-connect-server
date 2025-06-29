package com.example.solidconnection.security.aspect;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.security.annotation.RequireRoleAccess;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.example.solidconnection.common.exception.ErrorCode.ACCESS_DENIED;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleAuthorizationAspect {

    // todo: 추후 siteUserId로 파라미터 변경 시 수정 필요
    @Around("@annotation(requireRoleAccess)")
    public Object checkRoleAccess(ProceedingJoinPoint joinPoint, RequireRoleAccess requireRoleAccess) throws Throwable {
        SiteUser siteUser = null;
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof SiteUser) {
                siteUser = (SiteUser) arg;
                break;
            }
        }

        if (siteUser == null) {
            throw new CustomException(ACCESS_DENIED);
        }

        final SiteUser finalSiteUser = siteUser;
        Role[] allowedRoles = requireRoleAccess.roles();
        boolean hasAccess = Arrays.stream(allowedRoles)
                .anyMatch(role -> role.equals(finalSiteUser.getRole()));
        if (!hasAccess) {
            throw new CustomException(ACCESS_DENIED);
        }
        return joinPoint.proceed();
    }
}
