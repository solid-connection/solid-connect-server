package com.example.solidconnection.custom.resolver;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.security.userdetails.SiteUserDetails;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static com.example.solidconnection.custom.exception.ErrorCode.AUTHENTICATION_FAILED;

@Component
@RequiredArgsConstructor
public class AuthorizedUserResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthorizedUser.class)
                && parameter.getParameterType().equals(SiteUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        SiteUser siteUser = extractSiteUserFromAuthentication();
        if (parameter.getParameterAnnotation(AuthorizedUser.class).required() && siteUser == null) {
            throw new CustomException(AUTHENTICATION_FAILED, "로그인 상태가 아닙니다.");
        }

        return siteUser;
    }

    private SiteUser extractSiteUserFromAuthentication() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            SiteUserDetails principal = (SiteUserDetails) authentication.getPrincipal();
            return principal.getSiteUser();
        } catch (Exception e) {
            return null;
        }
    }
}
