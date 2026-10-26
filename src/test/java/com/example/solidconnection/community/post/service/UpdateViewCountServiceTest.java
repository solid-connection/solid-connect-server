package com.example.solidconnection.community.post.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.solidconnection.community.post.domain.ViewCountEvent;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.community.post.repository.ViewCountFlushRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("조회수 업데이트 서비스 테스트")
@ExtendWith(MockitoExtension.class)
class UpdateViewCountServiceTest {

    @InjectMocks
    private UpdateViewCountService updateViewCountService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ViewCountFlushRepository flushRepository;

    @Nested
    @DisplayName("처리 이력에 따른 조회수 반영")
    class 이벤트_반영 {

        @Test
        void 처리_이력이_있으면_DB_업데이트를_하지_않는다() {
            // given
            ViewCountEvent event = new ViewCountEvent("1-0", "flush-1", 1L, 2L);
            given(flushRepository.recordIfNew("flush-1", 1L, 2L)).willReturn(false);

            // when
            updateViewCountService.apply(event);

            // then
            then(postRepository).shouldHaveNoInteractions();
        }

        @Test
        void 신규_처리_이력이면_DB_조회수를_증가시킨다() {
            // given
            ViewCountEvent event = new ViewCountEvent("1-0", "flush-1", 1L, 2L);
            given(flushRepository.recordIfNew("flush-1", 1L, 2L)).willReturn(true);

            // when
            updateViewCountService.apply(event);

            // then
            then(postRepository).should().increaseViewCount(1L, 2L);
        }
    }
}
