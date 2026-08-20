package com.example.solidconnection.common.config.client;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .connectTimeout(TIMEOUT)
                .readTimeout(TIMEOUT)
                .build();
    }

    /*
     * - Discord webhook url 은 경로에 인증 토큰을 포함한다.
     * - RestTemplateBuilder 로 만든 RestTemplate 은 observation 이 적용되어 요청 url 이 메트릭 태그로 남을 수 있으므로,
     *   webhook 전송에는 observation 이 붙지 않는 별도 인스턴스를 사용한다.
     * */
    @Bean
    public RestTemplate discordWebhookRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) TIMEOUT.toMillis());
        requestFactory.setReadTimeout((int) TIMEOUT.toMillis());
        return new RestTemplate(requestFactory);
    }
}
