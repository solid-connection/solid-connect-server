package com.example.solidconnection.community.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.community.board.fixture.BoardFixture;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostCategory;
import com.example.solidconnection.community.post.domain.ViewCountEvent;
import com.example.solidconnection.community.post.fixture.PostFixture;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.community.post.repository.ViewCountStreamRepository;
import com.example.solidconnection.scheduler.UpdateViewCountScheduler;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestContainerSpringBootTest
@DisplayName("조회수 이벤트 복구 테스트")
class ViewCountRecoveryTest {

    @Autowired
    private PostFixture postFixture;

    @Autowired
    private BoardFixture boardFixture;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ViewCountStreamRepository stream;

    @Autowired
    private UpdateViewCountService updateService;

    @Autowired
    private ViewCountConsumer consumer;

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ScheduledAnnotationBeanPostProcessor scheduledTasks;

    @MockitoBean
    private UpdateViewCountScheduler scheduler;

    private List<Post> posts;

    @BeforeEach
    void setUp() {
        Board board = boardFixture.자유게시판();
        SiteUser user = siteUserFixture.사용자();
        posts = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            posts.add(postFixture.게시글("title", "content", false, PostCategory.자유, board, user));
        }
        stream.ensureGroup();
    }

    @Nested
    @DisplayName("DB 실패 및 완료 확인 실패 복구")
    class 장애_복구 {

        @Test
        void DB_업데이트_5건이_실패해도_재처리하면_5000회가_모두_반영된다() {
            // given: 선택한 게시글 5개의 조회수 UPDATE만 실패시키는 임시 제약조건
            List<ViewCountEvent> events = prepareEvents();
            String failingIds = events.subList(0, 5).stream().map(event -> Long.toString(event.postId()))
                    .collect(Collectors.joining(","));
            jdbc.execute("UPDATE post SET title = 'injected-view-failure' WHERE id IN (" + failingIds + ")");
            jdbc.execute("ALTER TABLE post ADD CONSTRAINT chk_view_count_failure "
                    + "CHECK (title <> 'injected-view-failure' OR view_count = 0)");
            try {
                // when: 5건은 처리 이력까지 롤백되고 PEL에 남아야 한다.
                events.forEach(consumer::process);
                assertThat(receipts()).isEqualTo(45);
                assertThat(streamPending()).isEqualTo(5);
                assertThat(jdbc.queryForObject("SELECT SUM(view_count) FROM post", Long.class)).isEqualTo(4500);
            } finally {
                jdbc.execute("ALTER TABLE post DROP CHECK chk_view_count_failure");
            }
            List<ViewCountEvent> recovered = stream.claim("after-crash", Duration.ZERO);
            assertThat(recovered).hasSize(5);
            recovered.forEach(consumer::process);

            // then: 총합, 게시글별 값, DB 이력 및 PEL 검증
            assertCounts(5000);
            assertThat(receipts()).isEqualTo(50);
            assertThat(streamPending()).isZero();
            assertThat(redis.opsForStream().size(ViewCountStreamRepository.STREAM_KEY)).isZero();
            System.out.println("RECOVERY input=5000 persisted=5000 lost=0 failedEvents=5");
        }

        @Test
        void 커밋_후_ACK_전_중단된_50건을_재인계해도_중복_반영하지_않는다() {
            // given: 실제 DB 커밋 후 ACK를 생략하여 종료 시점의 상태 재현
            List<ViewCountEvent> events = prepareEvents();
            events.forEach(updateService::apply);
            assertCounts(5000);
            assertThat(streamPending()).isEqualTo(50);

            // when: 다른 consumer가 같은 flushId로 회수하고 반복 처리
            List<ViewCountEvent> recovered = stream.claim("replacement", Duration.ZERO);
            assertThat(recovered).extracting(ViewCountEvent::flushId)
                    .containsExactlyInAnyOrderElementsOf(events.stream().map(ViewCountEvent::flushId).toList());
            recovered.forEach(consumer::process);
            recovered.forEach(consumer::process);

            // then
            assertCounts(5000);
            assertThat(receipts()).isEqualTo(50);
            assertThat(streamPending()).isZero();
            System.out.println("REPLAY committed=5000 afterReplay=5000 duplicated=0 events=50");
        }

        @Test
        void 커밋_후_ACK가_실패해도_재처리에서_중복_반영하지_않는다() {
            // given
            List<ViewCountEvent> events = prepareEvents();
            ViewCountStreamRepository failedAck = mock(ViewCountStreamRepository.class);
            doThrow(new IllegalStateException("injected ACK failure")).when(failedAck).acknowledge(events.getFirst());
            ViewCountConsumer failedConsumer = new ViewCountConsumer(failedAck, updateService, Duration.ZERO);
            // when: DB 커밋 성공 이후 ACK 호출만 실패
            failedConsumer.process(events.getFirst());
            assertThat(receipts()).isEqualTo(1);
            assertThat(streamPending()).isEqualTo(50);
            stream.claim("replacement", Duration.ZERO).forEach(consumer::process);
            // then
            assertCounts(5000);
            assertThat(receipts()).isEqualTo(50);
            assertThat(streamPending()).isZero();
        }
    }

    private List<ViewCountEvent> prepareEvents() {
        for (int i = 0; i < 50; i++) {
            for (int count = 0; count < 100; count++) {
                stream.increment(posts.get(i).getId());
            }
        }
        assertThat(redis.getExpire(ViewCountStreamRepository.ACTIVE_KEY)).isEqualTo(-1);
        assertThat(stream.flush()).isEqualTo(50);
        assertThat(stream.flush()).isZero();
        List<ViewCountEvent> events = stream.readNew("before-crash");
        assertThat(events).hasSize(50);
        return events;
    }

    private void assertCounts(long total) {
        long actualTotal = 0;
        for (int i = 0; i < 150; i++) {
            long actual = postRepository.getById(posts.get(i).getId()).getViewCount();
            assertThat(actual).as("post %s", i).isEqualTo(i < 50 ? 100 : 0);
            actualTotal += actual;
        }
        assertThat(actualTotal).isEqualTo(total);
    }

    @Nested
    @DisplayName("동일 이벤트 처리 및 증가와 집계의 동시성")
    class 동시성 {

        @Test
        void 동일_이벤트를_동시에_처리해도_한_번만_반영된다() throws Exception {
            // given
            long postId = posts.getFirst().getId();
            ViewCountEvent event = new ViewCountEvent("1-0", "concurrent-flush", postId, 100);
            try (var executor = Executors.newFixedThreadPool(8)) {
                List<Callable<Void>> tasks = new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    tasks.add(() -> {
                        updateService.apply(event);
                        return null;
                    });
                }
                // when: Future.get으로 스레드 예외도 검증
                for (var future : executor.invokeAll(tasks)) {
                    future.get();
                }
            }
            // then
            assertThat(postRepository.getById(postId).getViewCount()).isEqualTo(100);
            assertThat(receipts()).isEqualTo(1);
        }

        @Test
        void 증가와_flush를_동시에_실행해도_5000회가_보존된다() throws Exception {
            // given
            long postId = posts.getFirst().getId();
            try (var executor = Executors.newFixedThreadPool(6)) {
                List<Callable<Void>> tasks = new ArrayList<>();
                for (int thread = 0; thread < 5; thread++) {
                    tasks.add(() -> {
                        for (int i = 0; i < 1000; i++) {
                            stream.increment(postId);
                        }
                        return null;
                    });
                }
                tasks.add(() -> {
                    for (int i = 0; i < 100; i++) {
                        stream.flush();
                    }
                    return null;
                });
                // when
                for (var future : executor.invokeAll(tasks)) {
                    future.get();
                }
            }
            stream.flush();
            List<ViewCountEvent> events;
            while (!(events = stream.readNew("concurrent-consumer")).isEmpty()) {
                events.forEach(consumer::process);
            }
            // then
            assertThat(postRepository.getById(postId).getViewCount()).isEqualTo(5000);
            assertThat(streamPending()).isZero();
            assertThat(redis.opsForHash().size(ViewCountStreamRepository.ACTIVE_KEY)).isZero();
        }
    }

    @Nested
    @DisplayName("이벤트 저장 실패 시 활성 카운터 보존")
    class 이벤트_발행 {

        @Test
        void XADD가_실패하면_활성_카운터가_삭제되지_않는다() {
            // given: Stream 키 타입 오류
            redis.delete(ViewCountStreamRepository.STREAM_KEY);
            redis.opsForValue().set(ViewCountStreamRepository.STREAM_KEY, "wrong-type");
            stream.increment(posts.getFirst().getId());
            // when
            assertThatThrownBy(stream::flush).isInstanceOf(RuntimeException.class);
            // then
            assertThat(redis.opsForHash().get(ViewCountStreamRepository.ACTIVE_KEY,
                    posts.getFirst().getId().toString())).isEqualTo("1");
        }
    }

    @Nested
    @DisplayName("오류 이벤트 격리와 미확인 이벤트 순회")
    class 이벤트_소비와_재인계 {

        @Test
        void 잘못된_신규_이벤트가_있어도_같은_배치의_정상_이벤트는_처리한다() {
            // given
            redis.opsForStream().add(ViewCountStreamRepository.STREAM_KEY, Map.of("delta", "invalid"));
            long postId = posts.getFirst().getId();
            stream.increment(postId);
            stream.flush();
            // when
            consumer.consume();
            // then: 잘못된 이벤트는 유실시키지 않고 pending으로 보존
            assertThat(postRepository.getById(postId).getViewCount()).isEqualTo(1);
            assertThat(streamPending()).isEqualTo(1);
        }

        @Test
        void 잘못된_미확인_이벤트가_있어도_정상_이벤트를_재인계한다() {
            // given: 첫 read에서 정상 이벤트도 pending 상태가 된다.
            redis.opsForStream().add(ViewCountStreamRepository.STREAM_KEY, Map.of("delta", "invalid"));
            long postId = posts.getFirst().getId();
            stream.increment(postId);
            stream.flush();
            assertThat(stream.readNew("previous-consumer")).hasSize(1);
            // when
            stream.claim("replacement", Duration.ZERO).forEach(consumer::process);
            // then
            assertThat(postRepository.getById(postId).getViewCount()).isEqualTo(1);
            assertThat(streamPending()).isEqualTo(1);
        }

        @Test
        void 미확인_이벤트_150건을_커서로_끝까지_재인계한다() {
            // given
            posts.forEach(post -> stream.increment(post.getId()));
            for (int i = 0; i < 3; i++) {
                assertThat(stream.flush()).isEqualTo(50);
                assertThat(stream.readNew("previous-consumer")).hasSize(50);
            }
            assertThat(streamPending()).isEqualTo(150);
            // when: 다음 커서가 가리키는 위치부터 이어서 회수
            for (int i = 0; i < 3; i++) {
                List<ViewCountEvent> recovered = stream.claim("replacement", Duration.ZERO);
                assertThat(recovered).hasSize(50);
                recovered.forEach(consumer::process);
            }
            // then
            for (Post post : posts) {
                assertThat(postRepository.getById(post.getId()).getViewCount()).isEqualTo(1);
            }
            assertThat(receipts()).isEqualTo(150);
            assertThat(streamPending()).isZero();
        }
    }

    @Nested
    @DisplayName("마이그레이션과 삭제된 게시글 처리")
    class DB_반영 {

        @Test
        void 마이그레이션으로_생성한_테이블에서도_중복_flushId는_한번만_반영된다() {
            // given: Hibernate 생성 테이블을 실제 배포용 SQL로 교체
            jdbc.execute("DROP TABLE view_count_flush");
            new ResourceDatabasePopulator(new ClassPathResource("db/migration/V63__create_view_count_flush.sql"))
                    .execute(jdbc.getDataSource());
            ViewCountEvent event = new ViewCountEvent("1-0", "migration-flush", posts.getFirst().getId(), 100);
            // when
            updateService.apply(event);
            updateService.apply(event);
            // then
            assertThat(receipts()).isEqualTo(1);
            assertThat(postRepository.getById(event.postId()).getViewCount()).isEqualTo(100);
        }

        @Test
        void 게시글이_삭제된_이벤트도_완료하고_재처리하지_않는다() {
            // given
            long postId = posts.getFirst().getId();
            stream.increment(postId);
            stream.flush();
            postRepository.deleteById(postId);
            // when
            consumer.consume();
            // then
            assertThat(receipts()).isEqualTo(1);
            assertThat(streamPending()).isZero();
            assertThat(postRepository.findById(postId)).isEmpty();
        }
    }

    private long receipts() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM view_count_flush", Long.class);
    }

    private long streamPending() {
        return redis.opsForStream().pending(ViewCountStreamRepository.STREAM_KEY, ViewCountStreamRepository.GROUP)
                .getTotalPendingMessages();
    }

    @Nested
    @DisplayName("조회수 스케줄러 설정의 독립성")
    class 스케줄러_설정 {

        @Test
        void 조회수_스케줄러를_꺼도_다른_스케줄러는_등록된다() {
            // given
            String expectedTask = "com.example.solidconnection.scheduler.UserRemovalScheduler.scheduledUserRemoval";

            // when
            Set<ScheduledTask> registeredTasks = scheduledTasks.getScheduledTasks();

            // then
            assertThat(registeredTasks).extracting(Object::toString).contains(expectedTask);
        }
    }
}
