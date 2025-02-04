package com.example.solidconnection.custom.resolver;

import com.example.solidconnection.custom.security.userdetails.SiteUserDetails;
import com.example.solidconnection.siteuser.domain.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

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
                                  WebDataBinderFactory binderFactory) throws Exception {
        try {
            SiteUserDetails principal = (SiteUserDetails) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();
            return principal.getSiteUser();
        } catch (Exception e) {
            return null;
        }
    }
}
