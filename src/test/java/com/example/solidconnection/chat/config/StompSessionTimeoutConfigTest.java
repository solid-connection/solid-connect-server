package com.example.solidconnection.chat.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.task.VirtualThreadTaskExecutor;

@DisplayName("STOMP 세션 타임아웃 설정 테스트")
class StompSessionTimeoutConfigTest {

    @Test
    void 세션_종료_실행기는_기본_실행기_후보에서_제외한다() {
        // given
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(StompSessionTimeoutConfig.class)) {

            // when
            var beanDefinition = (AbstractBeanDefinition) context.getBeanFactory()
                    .getBeanDefinition("stompSessionCloseExecutor");
            var taskExecutor = context.getBean("stompSessionCloseExecutor");

            // then
            assertThat(beanDefinition.isDefaultCandidate()).isFalse();
            assertThat(taskExecutor).isInstanceOf(VirtualThreadTaskExecutor.class);
        }
    }
}
