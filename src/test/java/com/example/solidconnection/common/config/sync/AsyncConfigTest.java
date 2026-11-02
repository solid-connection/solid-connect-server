package com.example.solidconnection.common.config.sync;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.task.VirtualThreadTaskExecutor;

@DisplayName("비동기 실행기 설정 테스트")
class AsyncConfigTest {

    @Test
    void 채팅_Outbox는_전용_가상_스레드_실행기를_사용한다() {
        // given
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AsyncConfig.class)) {

            // when
            Object taskExecutor = context.getBean("chatMessageOutboxExecutor");

            // then
            assertThat(taskExecutor).isInstanceOf(VirtualThreadTaskExecutor.class);
        }
    }
}
