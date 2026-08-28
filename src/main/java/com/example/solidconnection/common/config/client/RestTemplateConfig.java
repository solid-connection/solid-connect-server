package com.example.solidconnection.common.config.client;

import java.time.Duration;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
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
     * - 메시지 편집에 PATCH 를 사용하는데, SimpleClientHttpRequestFactory 는 PATCH 를 지원하지 않으므로
     *   JDK HttpClient 기반 팩토리를 사용한다.
     * */
    @Bean
    public RestTemplate discordWebhookRestTemplate() {
        return new RestTemplate(ClientHttpRequestFactoryBuilder.jdk()
                .build(ClientHttpRequestFactorySettings.defaults()
                        .withConnectTimeout(TIMEOUT)
                        .withReadTimeout(TIMEOUT)));
    }

    @Bean
    public RestTemplate discordBotRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) TIMEOUT.toMillis());
        requestFactory.setReadTimeout((int) TIMEOUT.toMillis());
        return new RestTemplate(requestFactory);
    }
}
