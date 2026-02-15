---
name: review-pr
description: Pull Request를 체계적으로 리뷰하여 프로젝트 컨벤션 준수 여부와 코드 품질을 검증합니다
args: <PR 번호> (예: /review-pr 666)
---

# Pull Request 리뷰 가이드

이 skill은 solid-connect-server 프로젝트의 Pull Request를 체계적으로 리뷰합니다.

## 사용법

```bash
/review-pr <PR번호>
```

**예제:**
```bash
/review-pr 666
```

---

## 리뷰 프로세스

### 1단계: PR 정보 수집

GitHub CLI로 PR의 기본 정보와 변경사항을 파악합니다.

```bash
gh pr view <번호> -R solid-connection/solid-connect-server      # PR 기본 정보 조회
gh pr diff <번호> -R solid-connection/solid-connect-server      # 변경된 파일과 diff 확인
gh pr checks <번호> -R solid-connection/solid-connect-server    # CI/CD 상태 확인
```

**수집할 정보:**
- PR 제목 및 설명
- 관련 이슈 번호
- 변경된 파일 목록
- CI/CD 체크 상태

### 2단계: 변경 파일 분석

**도구 우선순위:**

1. **Serena MCP** (Java 코드 분석에 최적화)
   - `mcp__serena__get_symbols_overview <파일경로>` - 파일의 클래스/메서드 구조 파악
   - `mcp__serena__find_symbol <심볼명>` - 특정 심볼 검색
   - `mcp__serena__search_for_pattern <패턴>` - 컨벤션 위반 패턴 검색

2. **Read/Grep** (보조 분석)
   - `Read <파일경로>` - 파일 전체 읽기
   - `Grep --pattern <패턴>` - 패턴 검색

### 3단계: 체크리스트 검증

아래 체크리스트를 순서대로 확인합니다.

---

## 리뷰 체크리스트

각 항목의 상세 컨벤션은 참조 문서를 확인하세요.

### 1. 아키텍처 및 계층 구조

**검증 항목:**
- 계층형 아키텍처 준수 (Controller → Service → Repository)
- 역계층 참조 금지
- 순환 의존성 없음

👉 **참고:** `CLAUDE.md` - "아키텍처" 섹션

---

### 2. 네이밍 컨벤션

**검증 항목:**
- API 엔드포인트: kebab-case 사용 (예: `/user-profile`)
- DTO 변환 메서드: 단일 파라미터 `from()`, 다중 파라미터 `of()`
- Request/Response: `XXXRequest`, `XXXResponse` 형식
- 테스트 메서드: 한국어, `어떤_것을_하면_어떤_결과가_나온다()` 패턴

👉 **참고:** `CLAUDE.md` - "네이밍 컨벤션" 섹션

---

### 3. 코드 스타일

**검증 항목:**
- 와일드카드(`*`) import 금지
- 클래스 선언 전 빈 줄 존재
- private 메서드는 호출하는 public 메서드 바로 아래 위치
- Controller: 모든 파라미터 줄바꿈 필수
- 일반 메서드: 3개 이상 파라미터 시 줄바꿈
- 파일 끝 개행 문자

**패턴 검색 예제:**
```bash
mcp__serena__search_for_pattern "import.*\\*"  # 와일드카드 import 검색
```

👉 **참고:** `CLAUDE.md` - "코드 스타일" 섹션

---

### 4. Entity 및 JPA

**검증 항목:**
- 모든 필드에 `@Column` 어노테이션 존재
- `name` 속성으로 컬럼명 명시
- `nullable` 속성 명시
- null 불가: 원시 타입 (`int`, `long`, `boolean`)
- nullable: Wrapper 타입 (`Integer`, `Long`, `Boolean`)
- 양방향 연관관계 시 편의 메서드 존재

👉 **참고:** `CLAUDE.md` - "데이터베이스 접근" 섹션

---

### 5. 데이터베이스 마이그레이션

**검증 항목:**
- 스키마 변경 시 Flyway 마이그레이션 파일 추가
- 파일명 형식: `V{VERSION}__{DESCRIPTION}.sql`
- 위치: `src/main/resources/db/migration/`
- Entity 변경과 마이그레이션 일치
- 기존 마이그레이션 파일 수정 금지 (새 버전 생성)

👉 **참고:** `CLAUDE.md` - "데이터베이스 마이그레이션" 섹션

---

### 6. 테스트 코드

**검증 항목:**
- 새로운 Service/Repository 메서드에 대한 테스트 존재
- 예외 케이스 테스트 포함
- `@TestContainerSpringBootTest` 어노테이션 사용
- `@DisplayName`으로 한글 설명 제공
- `@Nested`로 기능별 그룹화
- Given-When-Then 구조 준수
- Fixture 패턴 사용 (FixtureBuilder + Fixture)

👉 **참고:** `.claude/skills/test/SKILL.md`

---

### 7. 커밋 메시지

**검증 항목:**
- `<type>: <description>` 형식
- Type: `feat`, `fix`, `refactor`, `test`, `chore`, `docs`, `perf`
- 간결하고 명확한 설명

👉 **참고:** `CLAUDE.md` - "Git 커밋 컨벤션" 섹션

---

### 8. 코드 품질 및 잠재적 이슈

**검증 항목:**
- 비즈니스 로직은 Service 계층에만
- Controller는 요청/응답 처리만
- `@Transactional` 적절하게 사용 (읽기 전용: `readOnly = true`)
- CustomException 사용
- N+1 쿼리 문제 없음
- 인증/인가 처리 (`@AuthorizedUser`)
- 민감 정보 노출 없음

👉 **참고:** `CLAUDE.md` - "아키텍처", "기술 스택 상세" 섹션

---

## 도구 사용 가이드

### Serena MCP (우선 사용)

```bash
# 파일의 클래스/메서드 구조 파악
mcp__serena__get_symbols_overview src/main/java/.../MentorService.java

# 특정 심볼 검색
mcp__serena__find_symbol "MentorDetailResponse"

# 컨벤션 위반 패턴 검색
mcp__serena__search_for_pattern "import.*\\*"
```

### GitHub CLI

```bash
# PR 정보
gh pr view 666 -R solid-connection/solid-connect-server --json title,body,author,number,url

# 변경사항
gh pr diff 666 -R solid-connection/solid-connect-server --patch

# CI 상태
gh pr checks 666 -R solid-connection/solid-connect-server
```

### 보조 도구

```bash
# 파일 읽기
Read src/main/java/.../MentorService.java

# 패턴 검색
Grep --pattern "@Column" --glob "*.java" --path src/main/java/.../domain
```

---

## 리뷰 결과 출력 형식

다음 형식으로 리뷰 결과를 정리하여 제공합니다.

```markdown
## PR 리뷰 결과: #{번호} - {제목}

**PR 링크:** {GitHub URL}
**관련 이슈:** #{이슈번호}

### 📊 PR 정보 요약

- **작성자:** {작성자}
- **변경 파일:** {숫자}개
- **추가 라인:** +{숫자}, **삭제 라인:** -{숫자}
- **CI/CD 상태:** {통과/실패}

### 주요 변경사항

{PR 설명 요약}

---

### ✅ 통과 항목

- 아키텍처 계층 구조 준수
- 네이밍 컨벤션 준수
- ...

### ⚠️ 개선 권장 항목

- **코드 스타일**: 와일드카드 import 사용
  - 파일: `src/main/java/.../MentorService.java:5`
  - 개선: 명시적 import로 변경

### ❌ 필수 수정 항목

- **Entity**: @Column 어노테이션 누락
  - 파일: `src/main/java/.../domain/Mentor.java:30`
  - 수정 방향: 모든 필드에 `@Column` 어노테이션 추가

---

### 💡 종합 의견

{전반적인 리뷰 의견}

**승인 상태:** ✅ 승인 / ⚠️ 조건부 승인 / ❌ 수정 후 재검토
```

---

## 리뷰 시 주의사항

1. **컨텍스트 이해 우선**: PR 설명과 관련 이슈를 먼저 읽고 변경의 목적 파악
2. **Serena MCP 우선 사용**: Java 코드 분석 시 효율적
3. **건설적 피드백**: 문제점 지적 시 구체적인 개선 방향 제시
4. **긍정적 피드백**: 잘된 부분도 언급하여 균형 잡힌 리뷰
5. **우선순위**: 아키텍처 > 네이밍 > 스타일 순으로 중요도 판단

---

## 참고 자료

- **프로젝트 컨벤션**: `CLAUDE.md` - 전체 개발 컨벤션
- **테스트 가이드**: `.claude/skills/test/SKILL.md` - 테스트 작성 가이드
- **개발 컨벤션 위키**: https://github.com/solid-connection/solid-connect-server/wiki/개발-컨벤션-정리
