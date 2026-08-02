---
name: load-universities
description: Load structured university application data into the Solid Connection dev environment through admin APIs, with read-only preflight and row-level verification.
---

# Load Universities

Use this skill when the user asks to ingest or upsert Solid Connection university data from a CSV or XLSX file.

## Scope

- Target only the approved dev API: `https://stage.solid-connection.com`.
- Use `/admin/**` APIs for authentication, entity reads, creation, update, and verification.
- Never use the legacy Markdown import endpoint.
- Never write credentials to repository files, reports, manifests, shell history examples, or final answers.
- Do not target local, prod, or an arbitrary URL.
- Do not mutate anything during preflight.

## Files

- Runner: `scripts/ingest_universities.py`
- CSV template: `templates/university_ingestion_template.csv`

The `.claude/skills/load-universities` and `.codex/skills/load-universities` copies must stay behaviorally identical.

## Input Schema

Required columns:

- `term_name`: term name in `YYYY-N` format.
- `home_university_name`
- `home_max_choice_count`: required when the home university does not already exist.
- `host_korean_name`
- `host_english_name`: required when the host university does not already exist.
- `host_format_name`: required when the host university does not already exist.
- `country_code`: required when the host university does not already exist.
- `region_code`: required when the host university does not already exist.

Optional columns:

- `univ_apply_info_id`: optional safety check. The runner primarily resolves existing rows by `termId + homeUniversityId + hostUniversityId`; when this ID is present it must match the resolved row.
- `home_email_domain`
- `student_capacity`
- `semester_available_for_dispatch`: enum such as `ONE_SEMESTER`, `TWO_SEMESTER`, `ONE_OR_TWO_SEMESTER`, `ONE_YEAR`, `IRRELEVANT`, `NO_DATA`.
- `semester_requirement`
- `details_for_language`
- `gpa_requirement`
- `gpa_requirement_criteria`
- `details_for_accommodation`
- `extra_info`: JSON object, or `key=value;key2=value2`.
- `language_requirements`: JSON array like `[{"languageTestType":"TOEFL_IBT","minScore":"80"}]`, JSON object like `{"TOEFL_IBT":"80"}`, or `TOEFL_IBT:80;IELTS:6.5`.
- `homepage_url`
- `english_course_url`
- `accommodation_url`
- `details_for_local`
- `logo_file`: local path or assets-dir relative path for missing host creation.
- `background_file`: local path or assets-dir relative path for missing host creation.

## Commands

Preflight only:

```bash
python3 .claude/skills/load-universities/scripts/ingest_universities.py \
  --mode preflight \
  --input path/to/universities.csv \
  --assets-dir path/to/assets \
  --admin-email "$SOLID_CONNECT_ADMIN_EMAIL" \
  --admin-password "$SOLID_CONNECT_ADMIN_PASSWORD"
```

Apply and verify:

```bash
python3 .claude/skills/load-universities/scripts/ingest_universities.py \
  --mode apply \
  --input path/to/universities.xlsx \
  --assets-dir path/to/assets \
  --admin-email "$SOLID_CONNECT_ADMIN_EMAIL" \
  --admin-password "$SOLID_CONNECT_ADMIN_PASSWORD"
```

Token-based authentication is also supported:

```bash
python3 .claude/skills/load-universities/scripts/ingest_universities.py \
  --mode apply \
  --input path/to/universities.csv \
  --access-token "$SOLID_CONNECT_ADMIN_ACCESS_TOKEN"
```

## Workflow

1. Validate the input file and dev base URL before authenticating.
2. Authenticate with either `--access-token` or admin email/password.
3. Parse every CSV/XLSX row and validate all required fields before mutation.
4. Read existing terms, home universities, and host universities through admin APIs.
5. If a host university is missing and either required image is absent, stop with JSON status `needs-assets`. This is a successful preflight result and performs zero mutations.
6. In `apply` mode, create missing terms, home universities, and host universities in dependency order. Existing terms, home universities, and host universities are reused and not modified.
7. Resolve existing `UnivApplyInfo` records with `GET /admin/univ-apply-infos?termId=&homeUniversityId=&hostUniversityId=`.
8. Fail on duplicate natural-key matches. Create absent `UnivApplyInfo` records and update existing records, including language requirements.
9. Re-fetch every touched `UnivApplyInfo` with `GET /admin/univ-apply-infos/{id}` and compare relation IDs, host Korean name, core fields, `extraInfo`, and language requirements.
10. Treat any mismatch as failure. Report created/reused/updated/failed counts and row-level failures.
