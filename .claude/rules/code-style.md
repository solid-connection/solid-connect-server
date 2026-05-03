---
paths:
  - "src/**/*.java"
---

# Code Style

## Naming
- DTO factory: `of(A, B)` for multiple params / `from(X)` for single param
- Request/Response classes: `XxxRequest`, `XxxResponse`
- REST endpoints: kebab-case only (`/user-profile`, NOT `/userProfile`)

## Formatting
- Blank line before every class declaration
- Newline at end of every file
- No wildcard imports
- Controller method params: always on separate lines
- 3+ params anywhere: always on separate lines
- Private methods: placed immediately below the public method that calls them

## Types
- Non-null: primitives (`int`, `long`, `boolean`)
- Nullable: wrapper types (`Integer`, `Long`, `Boolean`)
