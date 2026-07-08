package com.example.solidconnection.community.post.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.solidconnection.community.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
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
    private PostRedisManager postRedisManager;

    @Test
    void Redis_조회수_키가_이미_소비되었으면_DB_업데이트를_하지_않는다() {
        // given
        String key = "post:view:1";
        given(postRedisManager.getPostIdFromPostViewCountRedisKey(key)).willReturn(1L);
        given(postRedisManager.getAndDeleteViewCount(key)).willReturn(null);

        // when
        updateViewCountService.updateViewCount(key);

        // then
        then(postRepository).shouldHaveNoInteractions();
    }

    @Test
    void Redis_조회수가_있으면_DB_조회수를_증가시킨다() {
        // given
        String key = "post:view:1";
        given(postRedisManager.getPostIdFromPostViewCountRedisKey(key)).willReturn(1L);
        given(postRedisManager.getAndDeleteViewCount(key)).willReturn(2L);

        // when
        updateViewCountService.updateViewCount(key);

        // then
        then(postRepository).should().increaseViewCount(1L, 2L);
    }
}
