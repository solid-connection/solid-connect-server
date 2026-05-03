
# solid-connect-server

Exchange student support platform — Java 21, Spring Boot 3.5.x, MySQL, Redis, Flyway.

## Commands

```bash
./gradlew build                                               # build + flywayValidate
./gradlew test                                                # all tests
./gradlew test --tests <ClassName>                            # single class
./gradlew bootRun --args='--spring.profiles.active=local'
docker-compose -f docker-compose.local.yml up -d             # MySQL + Redis
```

## Architecture

Strict layered: Controller → Service → Repository/Domain. No reverse references.

- Controller: HTTP handling, input validation, response formatting
- Service: business logic, DTO conversion, @Transactional
- Repository: data access only
- Domain/Entity: domain logic only — no Service/Repository references
- DTO: request/response boundary only

Package root: `com.example.solidconnection.[domain]/{controller,service,domain,repository,dto}/`

## Conventions

@.claude/rules/code-style.md
@.claude/rules/testing.md
@.claude/rules/database.md

## Git

```
Commit : feat|fix|refactor|docs|test|chore|perf: <description>
Branch : type/issue-number-short-desc
```

## IMPORTANT: Do not violate these

- Flyway migrations are irreversible once deployed — create a new version, never edit
- QueryDSL Q-classes are auto-generated — never edit manually
- Tests must be fully independent — no shared state between tests
