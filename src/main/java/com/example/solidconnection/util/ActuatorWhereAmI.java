package com.example.solidconnection.util;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActuatorWhereAmI implements ApplicationRunner {
    private final Environment env;
    private final WebEndpointProperties webProps;

    @Override public void run(ApplicationArguments args) {
        String appPort = env.getProperty("local.server.port", "8080");
        String mgmtPort = env.getProperty("local.management.port", appPort);
        String ctxPath  = env.getProperty("server.servlet.context-path", "");
        String basePath = webProps.getBasePath(); // 기본 "/actuator" 또는 설정값
        System.out.printf("[ACTUATOR URL] http://localhost:%s%s%s/prometheus%n",
                          mgmtPort, ctxPath, basePath);
    }
}
