---
name: test
description: Generate a test class for a given service. Reads the implementation, checks existing fixtures, and produces a complete test file skeleton.
argument-hint: <ServiceClassName> (e.g. /test ChatService)
context: fork
allowed-tools:
  - Read
  - Bash
  - Grep
  - Glob
  - Write
  - Edit
---

# Generate Test Class

Target: $ARGUMENTS

## Step 1: Locate the service

```bash
find src/main/java -name "$ARGUMENTS.java"
```

Read the file. Identify:
- Package path
- All public methods (name, params, return type)
- Injected dependencies (repositories, other services)
- Thrown exceptions (`CustomException` with `ErrorCode`)

## Step 2: Identify required fixtures

For each entity type used as method input or returned from repositories:

```bash
find src/test -name "<Entity>Fixture.java"
find src/test -name "<Entity>FixtureBuilder.java"
```

- Fixture exists → note its Korean convenience methods for use in tests
- Fixture missing → flag it; offer to generate it after the test file

## Step 3: Determine test package and file path

```
src/test/java/com/example/solidconnection/[domain]/service/[ServiceName]Test.java
```

Derive `[domain]` from the service's package path.

## Step 4: Generate the test file

Follow every rule in `.claude/rules/testing.md`. Skeleton structure:

```java
package com.example.solidconnection.[domain].service;

// static imports first
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.support.TestContainerSpringBootTest;
// ... fixture and domain imports

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TestContainerSpringBootTest
@DisplayName("[ServiceName] 테스트")
class [ServiceName]Test {

    @Autowired
    private [ServiceName] [serviceField];

    // @Autowired repositories for direct state verification (not mocks)
    // @Autowired fixtures

    // shared test data as fields if reused across @Nested classes
    // private Entity entity;

    @BeforeEach
    void setUp() {
        // initialize shared fixtures here
    }

    @Nested
    class [메서드명_or_기능명] {

        @Test
        void [정상_케이스_한국어_메서드명]() {
            // given

            // when

            // then
        }

        @Test
        void [예외_케이스_한국어_메서드명]() {
            // given

            // when & then
            assertThatCode(() -> [service].[method](...))
                    .isInstanceOf(CustomException.class);
        }
    }
}
```

Rules to apply while generating:
- One `@Nested` class per public method (or per logical feature if a method is simple)
- Every `@Nested` class has at least one happy-path test and one exception test (if the method throws)
- Korean names on all `@Nested` classes and `@Test` methods
- `// given / // when / // then` comments on every test
- Use fixture convenience methods for test data — never construct entities manually
- No `@MockBean`; use real DB via TestContainers
- `@Autowired` repositories directly when you need to verify persisted state

## Step 5: Write the file

Write the generated test class to the correct path.

Then report:
- File created: `path/to/XxxServiceTest.java`
- Test methods generated: N (list them)
- Missing fixtures (if any): list entities that need FixtureBuilder + Fixture
