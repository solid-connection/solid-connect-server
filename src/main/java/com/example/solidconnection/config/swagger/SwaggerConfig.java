package com.example.solidconnection.config.swagger;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP;

@Configuration
@SecurityScheme(
        name = "access_token",
        type = HTTP,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        bearerFormat = "JWT",
        paramName = "Authorization",
        description = "엑세스 토큰을 입력하세요. (Bearer 포함 X)"
)
public class SwaggerConfig {

        public static final String ACCESS_TOKEN = "access_token";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("솔리드 커넥션 API 문서✈️")
                .description("솔리드 커넥션의 API 문서입니다. \n\"Autorization\" 버튼을 눌러 인증을 하면 인증이 필요한 API를 호출할 수 있습니다.")
                .version("1.0.0");
    }
}
