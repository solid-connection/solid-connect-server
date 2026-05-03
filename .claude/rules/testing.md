# Testing

Base annotation: `@TestContainerSpringBootTest` for all integration tests.

## Test data — Fixture pattern

```java
@TestComponent
@RequiredArgsConstructor
public class ChatRoomFixture {
    private final ChatRoomFixtureBuilder chatRoomFixtureBuilder;

    public ChatRoom 채팅방(boolean isGroup) { ... }        // Korean method names
    public ChatRoom 멘토링_채팅방(long mentoringId) { ... }
}
```

## Test class structure

```java
@TestContainerSpringBootTest
@DisplayName("XXX 서비스 테스트")
class XxxServiceTest {

    @Nested
    class 기능명 {

        @Test
        void 한국어_메서드명() {
            // given
            // when
            // then
        }
    }
}
```

- `@Nested` for grouping by feature
- Korean names on `@Nested` classes and `@Test` methods
- Given-When-Then with inline comments
- Tests are independent — no shared state across tests
