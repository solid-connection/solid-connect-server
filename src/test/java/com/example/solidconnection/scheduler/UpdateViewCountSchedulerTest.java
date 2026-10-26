package com.example.solidconnection.scheduler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import com.example.solidconnection.community.post.service.ViewCountConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("조회수 집계 스케줄러 테스트")
class UpdateViewCountSchedulerTest {

    @Mock
    private ViewCountConsumer consumer;

    @InjectMocks
    private UpdateViewCountScheduler scheduler;

    @Nested
    @DisplayName("조회수 집계의 실행 순서와 장애 복구")
    class 조회수_집계 {

        @Test
        void 활성_카운터를_이벤트로_옮긴_뒤_같은_주기에_소비한다() {
            // given
            InOrder order = inOrder(consumer);

            // when
            scheduler.updateViewCount();
            // then
            order.verify(consumer).flushActiveCounters();
            order.verify(consumer).consume();
        }

        @Test
        void flush가_실패해도_기존_이벤트의_소비와_복구를_시도한다() {
            // given
            doThrow(new IllegalStateException("injected flush failure")).when(consumer).flushActiveCounters();
            // when
            assertThatThrownBy(scheduler::updateViewCount).isInstanceOf(IllegalStateException.class);
            // then
            verify(consumer).consume();
        }
    }
}
