package com.example.solidconnection.common.interceptor;

import static com.example.solidconnection.common.exception.ErrorCode.BANNED_USER_ACCESS_DENIED;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.security.userdetails.SiteUserDetails;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.domain.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class BannedUserInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof SiteUserDetails) {
            SiteUserDetails userDetails = (SiteUserDetails) authentication.getPrincipal();
            SiteUser siteUser = userDetails.getSiteUser();

            if (siteUser.getUserStatus() == UserStatus.BANNED) {
                throw new CustomException(BANNED_USER_ACCESS_DENIED);
            }
        }
        return true;
    }
}