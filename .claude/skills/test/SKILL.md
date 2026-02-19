---
name: test
description: 테스트 코드를 작성하거나 수정할 때 이 프로젝트의 테스트 컨벤션과 패턴을 참고합니다
---

# 테스트 코드 작성 가이드

## 테스트 기본 설정

모든 통합 테스트는 `@TestContainerSpringBootTest` 어노테이션을 사용합니다.

```java
@TestContainerSpringBootTest
@DisplayName("채팅 서비스 테스트")
class ChatServiceTest {
    // 테스트 코드
}
```

**제공 기능:**
- MySQL, Redis 자동 실행
- Spring Boot 컨텍스트 로드
- 테스트 후 자동 DB 초기화
- JUnit 5 기반

## Fixture 패턴

테스트 데이터는 Fixture로 생성합니다 (FixtureBuilder + Fixture 패턴).

**위치:** `src/test/java/com/example/solidconnection/[domain]/fixture/`

```
fixture/
├── [Entity]FixtureBuilder.java    # Builder 패턴 구현
└── [Entity]Fixture.java            # 편의 메서드 제공
```

### 예제: ChatRoomFixtureBuilder

```java
@TestComponent
@RequiredArgsConstructor
public class ChatRoomFixtureBuilder {

    private final ChatRoomRepository chatRoomRepository;

    private boolean isGroup;
    private Long mentoringId;

    public ChatRoomFixtureBuilder chatRoom() {
        return new ChatRoomFixtureBuilder(chatRoomRepository);
    }

    public ChatRoomFixtureBuilder isGroup(boolean isGroup) {
        this.isGroup = isGroup;
        return this;
    }

    public ChatRoomFixtureBuilder mentoringId(long mentoringId) {
        this.mentoringId = mentoringId;
        return this;
    }

    public ChatRoom create() {
        ChatRoom chatRoom = new ChatRoom(mentoringId, isGroup);
        return chatRoomRepository.save(chatRoom);  // DB 저장
    }
}
```

### 예제: ChatRoomFixture

```java
@TestComponent
@RequiredArgsConstructor
public class ChatRoomFixture {

    private final ChatRoomFixtureBuilder chatRoomFixtureBuilder;

    // 편의 메서드: 기본값으로 생성
    public ChatRoom 채팅방(boolean isGroup) {
        return chatRoomFixtureBuilder.chatRoom()
                .isGroup(isGroup)
                .create();
    }

    public ChatRoom 멘토링_채팅방(long mentoringId) {
        return chatRoomFixtureBuilder.chatRoom()
                .mentoringId(mentoringId)
                .isGroup(false)
                .create();
    }
}
```

**편의 메서드 작성 팁:**

- 한국어 메서드명 사용 (가독성)
- 자주 사용되는 기본값 조합만 제공
- Builder를 조합하여 필요한 데이터 설정

### 테스트에서 사용

```java
@TestContainerSpringBootTest
class ChatServiceTest {

    @Autowired
    private ChatRoomFixture chatRoomFixture;

    @Test
    void 채팅방을_생성할_수_있다() {
        // 편의 메서드 사용
        ChatRoom room = chatRoomFixture.채팅방(false);

        // Builder 직접 사용
        ChatRoom customRoom = chatRoomFixture.chatRoomFixtureBuilder.chatRoom()
                .isGroup(true)
                .mentoringId(100L)
                .create();
    }
}
```

## 테스트 네이밍 컨벤션

### 테스트 메서드 네이밍 규칙

테스트 메서드명은 **한국어로 명확하게** 작성하며, 다음 패턴을 따릅니다:

#### 1. 정상 동작 테스트

```java
// 패턴: 어떤_것을_하면_어떤_결과가_나온다
@Test
void 채팅방이_없으면_빈_목록을_반환한다() { ... }

@Test
void 최신_메시지_순으로_정렬되어_조회한다() { ... }

@Test
void 참여자는_메시지를_전송할_수_있다() { ... }

@Test
void 페이징이_정상_작동한다() { ... }
```

#### 2. 예외 테스트

```java
// 패턴: 어떤_것을_하면_예외_응답을_반환한다
@Test
void 참여하지_않은_채팅방에_접근하면_예외_응답을_반환한다() { ... }

@Test
void 존재하지_않는_사용자로_메시지를_전송하면_예외_응답을_반환한다() { ... }

@Test
void 권한이_없으면_예외_응답을_반환한다() { ... }

@Test
void 필수_파라미터가_없으면_예외_응답을_반환한다() { ... }
```

## BDD 테스트 작성

테스트는 Given-When-Then 구조로 작성합니다.

```java
@Test
@DisplayName("최신 메시지순으로 채팅방 목록을 조회한다")
void 최신_메시지_순으로_조회한다() {
    // Given: 테스트 사전 조건
    SiteUser user = siteUserFixture.사용자();
    ChatRoom room1 = chatRoomFixture.채팅방(false);
    ChatRoom room2 = chatRoomFixture.채팅방(false);
    chatMessageFixture.메시지("오래된 메시지", user.getId(), room1);
    chatMessageFixture.메시지("최신 메시지", user.getId(), room2);

    // When: 실제 동작
    ChatRoomListResponse response = chatService.getChatRooms(user.getId());

    // Then: 결과 검증
    assertAll(
        () -> assertThat(response.chatRooms()).hasSize(2),
        () -> assertThat(response.chatRooms().get(0).id()).isEqualTo(room2.getId())
    );
}
```

## 테스트 그룹화 (@Nested)

기능별로 테스트를 그룹화합니다.

```java
@TestContainerSpringBootTest
class ChatServiceTest {

    @Nested
    @DisplayName("채팅방 목록 조회")
    class 채팅방_목록을_조회한다 {

        @Test
        void 빈_목록을_반환한다() { ... }

        @Test
        void 최신_메시지_순으로_조회한다() { ... }
    }

    @Nested
    @DisplayName("채팅 메시지 전송")
    class 채팅_메시지를_전송한다 {

        @BeforeEach
        void setUp() {
            // 이 그룹에만 적용되는 초기 설정
        }

        @Test
        void 참여자는_메시지를_전송할_수_있다() { ... }
    }
}
```

## 자주 사용하는 Assertion

```java
// 기본 검증
assertThat(value).isEqualTo(expected);
assertThat(value).isNotNull();

// 컬렉션
assertThat(list).hasSize(3);
assertThat(list).isEmpty();
assertThat(list).contains(item);

// 예외 검증
assertThatCode(() -> method())
    .isInstanceOf(CustomException.class)
    .hasMessage("error message");

// 복수 검증
assertAll(
    () -> assertThat(a).isEqualTo(1),
    () -> assertThat(b).isEqualTo(2)
);
```
