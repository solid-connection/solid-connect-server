package com.example.solidconnection.news.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "news")
public record NewsProperties(
        String defaultThumbnailUrl
) {

}
