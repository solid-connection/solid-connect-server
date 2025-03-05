package com.example.solidconnection.config.web;

import com.example.solidconnection.custom.resolver.AuthorizedUserResolver;
import com.example.solidconnection.custom.resolver.CustomPageableHandlerMethodArgumentResolver;
import com.example.solidconnection.custom.resolver.ExpiredTokenResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthorizedUserResolver authorizedUserResolver;
    private final ExpiredTokenResolver expiredTokenResolver;
    private final CustomPageableHandlerMethodArgumentResolver customPageableHandlerMethodArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.addAll(List.of(
                authorizedUserResolver,
                expiredTokenResolver,
                customPageableHandlerMethodArgumentResolver
        ));
    }
}
