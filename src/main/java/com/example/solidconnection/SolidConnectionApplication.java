package com.example.solidconnection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConfigurationPropertiesScan
@EnableScheduling
@EnableJpaAuditing
@EnableCaching
@SpringBootApplication
public class SolidConnectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(SolidConnectionApplication.class, args);
    }

}
