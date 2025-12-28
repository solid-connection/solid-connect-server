package com.example.solidconnection.common.config.web;

import com.example.solidconnection.common.interceptor.BannedUserInterceptor;
import com.example.solidconnection.common.resolver.AuthorizedUserResolver;
import com.example.solidconnection.common.resolver.CustomPageableHandlerMethodArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthorizedUserResolver authorizedUserResolver;
    private final CustomPageableHandlerMethodArgumentResolver customPageableHandlerMethodArgumentResolver;
    private final BannedUserInterceptor bannedUserInterceptor;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.addAll(List.of(
                authorizedUserResolver,
                customPageableHandlerMethodArgumentResolver
        ));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(bannedUserInterceptor)
                .addPathPatterns("/posts/**", "/comments/**", "/chats/**", "/community/**");
    }
}
