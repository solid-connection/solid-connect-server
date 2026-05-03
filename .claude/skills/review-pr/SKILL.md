---
name: review-pr
description: Fetch a GitHub PR and review it against project conventions and code quality
args: <PR number> (e.g. /review-pr 666)
context: fork
allowed-tools:
  - Bash
  - Read
  - Grep
  - Glob
---

# PR Review

## Step 1: Fetch PR data

```bash
gh pr view $ARGUMENTS -R solid-connection/solid-connect-server --json title,body,author,number,url,headRefName
gh pr diff $ARGUMENTS -R solid-connection/solid-connect-server
gh pr checks $ARGUMENTS -R solid-connection/solid-connect-server
```

## Step 2: Review checklist

Work through each area in order. For each finding, record: file:line / problem / suggestion.

### Architecture
- Controller → Service → Repository only. No reverse references.
- Business logic must not leak into Controller or Repository.
- Ref: `CLAUDE.md` - Architecture

### Naming
- REST endpoints: kebab-case (`/user-profile`, not `/userProfile`)
- DTO factory: `from(X)` single param / `of(A, B)` multiple params
- Classes: `XxxRequest`, `XxxResponse`
- Ref: `.claude/rules/code-style.md`

### Code style
- No wildcard imports
- Blank line before class declaration
- Controller params: always on separate lines; 3+ params anywhere: separate lines
- Private methods: immediately below the calling public method
- Ref: `.claude/rules/code-style.md`

### Entity / JPA
- Every field: `@Column(name = "...", nullable = ...)`
- Non-null → primitives; nullable → wrapper types
- `@Table(name = "...")` on every entity
- Ref: `.claude/rules/database.md`

### Flyway
- Schema changes must have a matching migration file
- Naming: `V{VERSION}__{DESCRIPTION}.sql`
- Existing migration files must not be modified
- Ref: `.claude/rules/database.md`

### Tests
- New service/repository methods must have tests
- `@TestContainerSpringBootTest`, `@Nested`, Korean method names, Given-When-Then
- Fixture pattern: FixtureBuilder + Fixture
- Ref: `.claude/skills/test/SKILL.md`

### Commit messages
- Format: `type: description` where type is feat|fix|refactor|docs|test|chore|perf
- Ref: `CLAUDE.md` - Git

### Code quality
- `@Transactional(readOnly = true)` on read-only service methods
- Use `CustomException` — no raw `RuntimeException`
- Check for N+1 queries (missing fetch join or `@BatchSize`)
- No sensitive data exposed in responses

## Step 3: Output format

```
## PR Review: #{number} - {title}

Link: {url}
Author: {author}  Branch: {branch}  CI: {pass/fail}

### Summary
{brief description of what the PR does}

### Passed
- {item}

### Suggestions (non-blocking)
- {file:line} — {problem} — {suggestion}

### Required changes
- {file:line} — {problem} — {fix direction}

### Overall
{conclusion}

Verdict: Approved / Approved with suggestions / Changes requested
```
