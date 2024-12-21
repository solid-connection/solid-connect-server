package com.example.solidconnection.config.cors;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "cors")
@Configuration
public class CorsPropertiesConfig {

    private List<String> allowedOrigins;
}
