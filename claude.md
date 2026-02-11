
# CLAUDE.md

이 파일은 Claude Code가 solid-connect-server 저장소에서 작업할 때 참고하는 가이드입니다.

## 프로젝트 개요

Solid Connect Server는 교환학생 준비생을 위해 대학 정보, 멘토 매칭, 모의지원 기능 등을 제공하는 교환학생 지원 통합 플랫폼입니다.

- **언어**: Java 17
- **프레임워크**: Spring Boot 3.1.5
- **빌드 도구**: Gradle
- **데이터베이스**: MySQL (주), Redis (캐싱)
- **마이그레이션**: Flyway

---

## 빌드 및 개발 명령어

### Gradle 빌드 명령어

```bash
# 전체 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 특정 테스트만 실행
./gradlew test --tests ChatServiceTest

# 애플리케이션 실행
./gradlew bootRun

# 로컬 개발 환경 시작 (MySQL, Redis)
docker-compose -f docker-compose.local.yml up -d
```

### 프로필별 실행

```bash
# 로컬 개발 환경
./gradlew bootRun --args='--spring.profiles.active=local'

# 개발 환경
./gradlew bootRun --args='--spring.profiles.active=dev'

# 운영 환경
./gradlew bootRun --args='--spring.profiles.active=prod'
```

---

## 프로젝트 구조

```
solid-connect-server/
├── src/
│   ├── main/
│   │   ├── java/com/example/solidconnection/
│   │   │   ├── [domain]/                    # 도메인별 폴더
│   │   │   │   ├── controller/             # REST API 엔드포인트
│   │   │   │   ├── service/                # 비즈니스 로직
│   │   │   │   ├── domain/                 # JPA Entity
│   │   │   │   ├── repository/             # 데이터 접근 계층
│   │   │   │   └── dto/                    # DTO (Request/Response)
│   │   │   └── common/                     # 공통 기능
│   │   │       ├── exception/              # 커스텀 예외
│   │   │       ├── config/                 # Spring 설정
│   │   │       └── util/                   # 유틸리티
│   │   └── resources/
│   │       ├── db/migration/               # Flyway 마이그레이션
│   │       └── application*.yml            # 설정 파일
│   └── test/
│       └── java/com/example/solidconnection/
│           ├── [domain]/fixture/           # 테스트 Fixture
│           ├── [domain]/service/           # 서비스 테스트
│           ├── support/                    # 테스트 설정
│           └── ...
├── docker-compose.local.yml                # 로컬 컨테이너
├── docker-compose.dev.yml                  # 개발 컨테이너
├── docker-compose.prod.yml                 # 운영 컨테이너
├── Dockerfile                              # 이미지 빌드
├── build.gradle                            # Gradle 설정
```

---

## 아키텍처

### 계층형 아키텍처 (Layered Architecture)

각 계층은 자신의 바로 아래 계층만 참조할 수 있습니다.

```
Controller → Service → Repository/Domain
```

**각 계층의 역할:**

- **Controller**: HTTP 요청 처리, 입력값 검증, 응답 포맷팅
- **Service**: 비즈니스 로직 처리, DTO 변환, 트랜잭션 관리
- **Repository**: 데이터 접근 계층, DB 쿼리 작성
- **Domain (Entity)**: JPA 엔티티, 도메인 모델

**주요 규칙:**

- ✅ 역계층 참조 금지 (예: Repository에서 Service 참조 불가)
- ✅ Service는 Repository를 주입받아 사용
- ✅ Controller는 Service를 주입받아 사용
- ✅ Entity는 도메인 로직만 포함
- ✅ DTO는 요청/응답 시에만 사용

### 패키지 구조

```
[domain]/
├── controller/          # REST API 엔드포인트
├── service/            # 비즈니스 로직 (Service)
├── domain/             # JPA Entity
├── repository/         # 데이터 접근 계층 (Repository)
└── dto/                # DTO (Request/Response)
```

---

## 개발 컨벤션

### 코드 스타일

프로젝트의 개발 컨벤션을 따릅니다: [개발-컨벤션-정리](https://github.com/solid-connection/solid-connect-server/wiki/개발-컨벤션-정리)

**주요 규칙:**

- **클래스 선언 전 줄바꿈**: 클래스 정의 앞에 빈 줄 필수
- **파일 끝 줄바꿈**: 모든 파일은 개행 문자로 종료
- **와일드카드 import 금지**: 명시적 import만 사용
- **파라미터 줄바꿈**: Controller는 필수, 3개 이상의 파라미터가 있으면 줄바꿈
- **private 메서드 위치**: 호출하는 public 메서드 바로 아래 위치
- **원시 타입 사용**: null이 아닌 값은 `int`, `long` 등 원시 타입 사용, nullable은 Wrapper 사용
- **JPA @Column**: Entity의 모든 필드에 `@Column` 속성과 필드명 지정

### 네이밍 컨벤션

```java
// DTO 변환
// 다중 파라미터: of() 메서드
public static UserDto of(User user, Profile profile) { ... }

// 단일 파라미터: from() 메서드
public static UserDto from(User user) { ... }

// API 요청/응답
// XXXRequest, XXXResponse 형식
public class UserCreateRequest { ... }
public class UserCreateResponse { ... }

// REST API 엔드포인트
// kebab-case 사용
@GetMapping("/user-profile")      // O
@GetMapping("/userProfile")       // X
```

---

## 기술 스택 상세

### Core Framework

- **Spring Boot 3.1.5**: 스프링 부트
- **Spring Security**: JWT 기반 인증
- **Spring Data JPA**: ORM
- **QueryDSL**: 동적 쿼리 생성

### 데이터베이스

- **MySQL**: 주 데이터베이스
- **Redis**: 캐싱 저장소
- **Flyway**: 데이터베이스 버전 관리

### 모니터링 & 보안

- **Spring Boot Actuator**: 애플리케이션 모니터링
- **Prometheus**: 메트릭 수집
- **Sentry**: 에러 추적
- **JWT**: JWT 토큰 관리

### 개발 도구

- **Lombok**: 보일러플레이트 코드 감소
- **AWS S3 SDK**: 파일 저장소
- **WebSocket**: 실시간 통신
- **TestContainers**: 통합 테스트용 컨테이너

---

## 테스트 코드 작성

테스트 작성 시 `/test` skill을 참고하세요. (테스트 관련 작업 시 자동으로 로드됩니다)

- `@TestContainerSpringBootTest` 기반 통합 테스트
- FixtureBuilder + Fixture 패턴으로 테스트 데이터 생성
- 한국어 메서드명, Given-When-Then 구조, @Nested 그룹화

---

## Git 커밋 컨벤션

### 형식

```
<type>: <description>

[optional body]
```

### Type 목록

```
feat:     새로운 기능 추가
fix:      버그 수정
refactor: 코드 리팩토링 (기능 변경 없음)
docs:     문서 변경
test:     테스트 추가/수정
chore:    빌드 설정, 패키지 관리
perf:     성능 개선
```

### 예제

```bash
# 기능 추가
feat: 대학 검색 기능 추가

# 버그 수정
fix: 채팅방 조회 시 정렬 버그 수정

# 리팩토링
refactor: ChatService 메서드 분리

# 테스트 추가
test: ChatService 테스트 케이스 추가

# 브랜치명
refactor/529-shortening-cd-time
```

---

## 데이터베이스 마이그레이션

### Flyway 사용

모든 DB 스키마 변경사항은 Flyway로 관리합니다.

**위치:** `src/main/resources/db/migration/`

**파일명 형식:** `V{VERSION}__{DESCRIPTION}.sql`

```
V1__init_schema.sql
V2__add_chat_table.sql
V3__add_user_role_column.sql
```

### 마이그레이션 추가

1. `V{next_version}__{description}.sql` 파일 생성
2. SQL 작성
3. `./gradlew build` 시 자동 검증 (flywayValidate)

**주의:** 한 번 배포된 마이그레이션은 수정 불가 (새 버전으로 생성)

---

## 데이터베이스 접근

### JPA Entity

```java
@Entity
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "is_group", nullable = false)
    private boolean isGroup;

    @Column(name = "mentoring_id", nullable = true)
    private Long mentoringId;
}
```

**규칙:**
- `@Column` 필수 (모든 필드)
- 필드명과 DB 컬럼명 일치
- nullable 명시

### Repository

```java
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByMentoringId(Long mentoringId);
    List<ChatRoom> findByIsGroup(boolean isGroup);
}
```

---

## 주요 파일 위치

| 파일/폴더 | 설명 |
|----------|------|
| `src/main/java/com/example/solidconnection/` | 메인 소스 코드 |
| `src/test/java/com/example/solidconnection/` | 테스트 코드 |
| `src/main/resources/db/migration/` | Flyway 마이그레이션 |
| `src/main/resources/application.yml` | 공통 설정 |
| `docker-compose.*.yml` | 환경별 도커 설정 |
| `build.gradle` | Gradle 빌드 설정 |

---

### 프로필
- **local**: Development with embedded Tomcat
- **dev**: Development server (stage.solid-connection.com)
- **prod**: Production server (solid-connection.com)

---

## 자주하는 작업

### 새 기능 추가

1. Entity 생성 (`src/main/java/.../domain/`)
2. Repository 작성 (`src/main/java/.../repository/`)
3. Service 구현 (`src/main/java/.../service/`)
4. Controller 작성 (`src/main/java/.../controller/`)
5. DTO 정의 (`src/main/java/.../dto/`)
6. Flyway 마이그레이션 작성
7. 테스트 코드 작성

### 테스트 작성

1. FixtureBuilder 생성 (필요시)
2. Fixture 편의 메서드 추가 (필요시)
3. 테스트 클래스 작성 (`*Test.java`)
4. @Nested로 테스트 그룹화
5. Given-When-Then 구조로 작성
6. `./gradlew test` 실행

### DB 스키마 변경

1. `V{next}__{description}.sql` 파일 생성
2. 마이그레이션 SQL 작성
3. Entity 업데이트 (필요시)
4. 테스트 실행

---

## 참고 자료

- **개발 컨벤션**: https://github.com/solid-connection/solid-connect-server/wiki/개발-컨벤션-정리
- **테스트 가이드**: `test.md` 파일 참고
- **Spring Boot**: https://spring.io/projects/spring-boot
- **JPA**: https://spring.io/projects/spring-data-jpa
- **TestContainers**: https://www.testcontainers.org/
- **Flyway**: https://flywaydb.org/

---


## 주의사항

1. **Flyway 마이그레이션은 되돌릴 수 없음** - 신중하게 작성
2. **QueryDSL Q클래스는 자동 생성** - 수동 수정 금지
3. **테스트는 독립적** - 테스트 간 데이터 공유 불가
4. **환경별 설정 분리** - application-local.yml, application-dev.yml, application-prod.yml
5. **한국어 메서드명** - 테스트 가독성 향상을 위해 사용
