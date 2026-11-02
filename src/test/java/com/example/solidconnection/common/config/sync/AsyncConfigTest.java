package com.example.solidconnection.common.config.sync;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@DisplayName("비동기 실행기 설정 테스트")
class AsyncConfigTest {

    @Test
    void 채팅_Outbox_실행기는_동시_실행과_대기_작업에_상한을_둔다() {
        // given
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AsyncConfig.class)) {

            // when
            ThreadPoolTaskExecutor taskExecutor = context.getBean(
                    "chatMessageOutboxExecutor",
                    ThreadPoolTaskExecutor.class
            );

            // then
            assertThat(taskExecutor.getMaxPoolSize()).isEqualTo(4);
            assertThat(taskExecutor.getThreadPoolExecutor().getQueue().remainingCapacity()).isEqualTo(100);
            assertThat(taskExecutor.getThreadPoolExecutor().getRejectedExecutionHandler())
                    .isInstanceOf(ThreadPoolExecutor.DiscardPolicy.class);
        }
    }
}
