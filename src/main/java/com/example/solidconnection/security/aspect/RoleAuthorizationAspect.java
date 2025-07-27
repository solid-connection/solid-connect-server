package com.example.solidconnection.security.aspect;

import static com.example.solidconnection.common.exception.ErrorCode.ACCESS_DENIED;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.security.annotation.RequireRoleAccess;
import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleAuthorizationAspect {

    private final SiteUserRepository siteUserRepository;

    // todo: 추후 개선 필요
    @Around("@annotation(requireRoleAccess)")
    public Object checkRoleAccess(ProceedingJoinPoint joinPoint, RequireRoleAccess requireRoleAccess) throws Throwable {

        Long siteUserId = extractAuthorizedUserId(joinPoint);

        if (siteUserId == null) {
            throw new CustomException(ACCESS_DENIED);
        }

        SiteUser siteUser = siteUserRepository.findById(siteUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        validateUserRole(siteUser, requireRoleAccess.roles());

        return joinPoint.proceed();
    }

    private Long extractAuthorizedUserId(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(AuthorizedUser.class)) {
                Object arg = args[i];
                if (arg instanceof Long) {
                    return (Long) arg;
                } else if (parameters[i].getType() == long.class) {
                    return (Long) arg;
                }
            }
        }
        return null;
    }

    private void validateUserRole(SiteUser siteUser, Role[] allowedRoles) {
        boolean hasAccess = Arrays.asList(allowedRoles).contains(siteUser.getRole());

        if (!hasAccess) {
            throw new CustomException(ACCESS_DENIED);
        }
    }
}
