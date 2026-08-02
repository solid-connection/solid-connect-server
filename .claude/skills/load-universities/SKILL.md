---
name: load-universities
description: Safely prepare and apply an AI-reviewed university import from an arbitrary source XLSX file.
---

# Load Universities

Use this skill when asked to load university exchange information from a university-provided XLSX file. Source workbooks are not required to have a stable layout: sheets, header rows, merged cells, column names, and notice rows may differ for every upload.

## Scope and Safety

- Before any workbook inspection, authentication, or API request, ask the operator to choose the target environment: `local` or `stage`. Do not infer or reuse an environment from an earlier import.
- Target only the selected environment through `/admin/**` APIs: `local` uses `http://localhost:8080`; `stage` uses `https://stage.solid-connection.com`. Never target prod or an arbitrary URL.
- Do not create or maintain parsers, mappings, templates, or configuration keyed by a home university, term, workbook layout, sheet, or header.
- Treat the workbook as evidence, not as an API payload. The agent interprets it for this one import and prepares a transient canonical payload only after review.
- Never mutate data while extraction questions are unresolved. Require an explicit user confirmation to apply the entire file.
- Before any admin API lookup, establish authentication. After the operator has selected the environment, if no authenticated admin session or token is available, ask for the administrator email and password as the next focused question; do not proceed with an unauthenticated API probe instead.
- Use supplied credentials only to sign in to the selected environment's `/admin/auth/sign-in` endpoint and retain the resulting access token only in process memory for this import. Never put credentials or access/refresh tokens in a command line, file, environment file, payload, report, browser page, or final answer.
- If sign-in fails, the selected endpoint redirects away from `/admin/**`, or authenticated read-only requests cannot be made, stop and report the exact access blocker. Do not substitute another host or infer an API base URL.
- For a missing `HostUniversity`, image candidates may come only from the university's official website or Wikipedia. Show the image and source URL. Any other or uncertain source is a blocking question.
- Keep the source workbook unchanged. Do not overwrite it or ask the operator to convert it into a template.

## Workflow

1. Ask the operator to select `local` or `stage`. Use only that environment's prescribed base URL for the rest of the import.
2. Establish an authenticated admin session for the selected environment. If credentials are unavailable, ask for the administrator email and password before inspecting the workbook or calling any admin endpoint. Sign in once, keep the access token only in memory, and use it only for this import.
3. Inspect every workbook sheet before drawing conclusions. Identify data tables, header rows, merged-cell values, footnotes, excluded rows, and the source locations supporting each extracted value.
4. Infer the proposed `term_name` and `home_university_name` from the workbook/file context. Extract candidate `HostUniversity` and `UnivApplyInfo` records from relevant rows only.
5. Resolve existing terms, home universities, host universities, and application rows through authenticated, read-only admin API calls. Do not mutate yet.
6. For every missing, ambiguous, conflicting, or low-confidence value, ask one focused question. Examples include a university identity match, country/region code, capacity meaning, language requirement interpretation, and image source. Do not guess.
7. Find required logo/background candidates for new host universities from the allowed sources. If no suitable candidate exists, ask for an image; do not apply the file.
8. Present one file-level review containing:
   - source file identity and every relevant sheet/cell or range;
   - extracted values and unresolved-question status;
   - existing-match decision and planned create/update/delete action per university;
   - image preview/source URL for each new host university;
   - counts for the previous scope, extracted records, creates, updates, deletes, and blockers.
9. Treat the file as the complete snapshot for its `home university + term`. Existing `UnivApplyInfo` records in that scope absent from the approved review are deletion candidates. Show them before asking for confirmation.
10. Only after the user explicitly approves the whole review, create the transient canonical payload required by `scripts/ingest_universities.py`, run its preflight, then apply and re-fetch verification. Delete only the reviewed stale rows and report every outcome. If the existing API cannot delete a referenced record, report the blocking record and failed snapshot; do not conceal partial results.
11. Report the previous, extracted, created, updated, deleted, skipped, and failed counts with row-level verification results and source references.

## Existing Runner

`scripts/ingest_universities.py` is a dev-only final upsert helper for the transient canonical payload. It is not an arbitrary-workbook parser and must never be given a raw source XLSX unless that file already happens to use its canonical schema.

Use `--mode preflight` before `--mode apply`. Provide local logo/background files only after their sources have been reviewed. The runner's structured-row verification remains mandatory, but it does not replace the file-level review above.

## Canonical Payload Fields

The agent may create a temporary payload for the runner with `term_name`, `home_university_name`, `home_max_choice_count`, `host_korean_name`, `host_english_name`, `host_format_name`, `country_code`, `region_code`, capacities, requirements, URLs, and image paths. This is an internal handoff only; it must not be requested from the user as a prerequisite and should be removed from temporary storage after the run when safe.

## Stop Conditions

- Stop before any workbook inspection, authentication, or API access until the operator selects `local` or `stage`.
- Stop before workbook extraction or API access when administrator credentials have not been provided and no authenticated session is available; ask for the administrator email and password.
- Stop after a failed sign-in or inaccessible selected endpoint; never try a different host as a workaround.
- Stop before mutation when a question, identity match, image source, or required field remains unresolved.
- Stop when an image candidate is not from an official university website or Wikipedia.
- Stop and show the full review when the user has not explicitly approved the entire file.
- Treat a failed post-apply verification or a failed reviewed deletion as a failed import and report the exact affected records.
