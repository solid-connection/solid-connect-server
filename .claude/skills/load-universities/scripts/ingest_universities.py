#!/usr/bin/env python3
"""Dev-only Solid Connection university ingestion runner."""

from __future__ import annotations

import argparse
import csv
import getpass
import json
import mimetypes
import os
import re
import ssl
import sys
import uuid
import zipfile
from dataclasses import dataclass
from pathlib import Path
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen
from xml.etree import ElementTree


APPROVED_DEV_BASE_URL = "https://stage.solid-connection.com"
TERM_RE = re.compile(r"^\d{4}-\d$")
ALLOWED_SEMESTERS = {
    "ONE_SEMESTER",
    "TWO_SEMESTER",
    "FOUR_SEMESTER",
    "ONE_OR_TWO_SEMESTER",
    "ONE_YEAR",
    "IRRELEVANT",
    "NO_DATA",
}
LANGUAGE_TEST_TYPES = {
    "CEFR",
    "JLPT",
    "DALF",
    "DELF",
    "DELE",
    "DUOLINGO",
    "IELTS",
    "NEW_HSK",
    "TCF",
    "TEF",
    "TOEFL_IBT",
    "TOEFL_ITP",
    "TOEIC",
    "ETC",
}


class IngestionError(Exception):
    pass


@dataclass(frozen=True)
class ParsedRow:
    row_number: int
    term_name: str
    home_university_name: str
    home_max_choice_count: int | None
    home_email_domain: str | None
    host_korean_name: str
    host_english_name: str | None
    host_format_name: str | None
    country_code: str | None
    region_code: str | None
    univ_apply_info_id: int | None
    student_capacity: int | None
    semester_available_for_dispatch: str | None
    semester_requirement: str | None
    details_for_language: str | None
    gpa_requirement: str | None
    gpa_requirement_criteria: str | None
    details_for_accommodation: str | None
    extra_info: dict[str, str]
    language_requirements: list[dict[str, str]]
    homepage_url: str | None
    english_course_url: str | None
    accommodation_url: str | None
    details_for_local: str | None
    logo_file: str | None
    background_file: str | None


def clean(value: Any) -> str:
    return "" if value is None else str(value).strip()


def empty_to_none(value: Any) -> str | None:
    text = clean(value)
    return text or None


def parse_int(value: Any, field: str, row_number: int) -> int | None:
    text = clean(value)
    if not text:
        return None
    try:
        return int(float(text))
    except ValueError as exc:
        raise IngestionError(f"row {row_number}: {field} must be an integer") from exc


def require(value: Any, field: str, row_number: int) -> str:
    text = clean(value)
    if not text:
        raise IngestionError(f"row {row_number}: missing required field {field}")
    return text


def parse_extra_info(value: Any, row_number: int) -> dict[str, str]:
    text = clean(value)
    if not text:
        return {}
    if text.startswith("{"):
        parsed = json.loads(text)
        if not isinstance(parsed, dict):
            raise IngestionError(f"row {row_number}: extra_info JSON must be an object")
        return {str(k): "" if v is None else str(v) for k, v in parsed.items()}
    result: dict[str, str] = {}
    for part in text.split(";"):
        if not part.strip():
            continue
        if "=" not in part:
            raise IngestionError(f"row {row_number}: extra_info entry must be key=value")
        key, item_value = part.split("=", 1)
        result[key.strip()] = item_value.strip()
    return result


def parse_language_requirements(value: Any, row_number: int) -> list[dict[str, str]]:
    text = clean(value)
    if not text:
        return []
    if text.startswith("[") or text.startswith("{"):
        parsed = json.loads(text)
        if isinstance(parsed, dict):
            items = [
                {"languageTestType": str(k), "minScore": str(v)}
                for k, v in parsed.items()
            ]
        elif isinstance(parsed, list):
            items = parsed
        else:
            raise IngestionError(f"row {row_number}: language_requirements JSON must be an object or array")
    else:
        items = []
        for part in text.split(";"):
            if not part.strip():
                continue
            if ":" not in part:
                raise IngestionError(f"row {row_number}: language requirement must be TYPE:score")
            test_type, min_score = part.split(":", 1)
            items.append({"languageTestType": test_type.strip(), "minScore": min_score.strip()})

    normalized = []
    for item in items:
        if not isinstance(item, dict):
            raise IngestionError(f"row {row_number}: each language requirement must be an object")
        test_type = clean(item.get("languageTestType"))
        min_score = clean(item.get("minScore"))
        if test_type not in LANGUAGE_TEST_TYPES:
            raise IngestionError(f"row {row_number}: unsupported language test type {test_type}")
        if not min_score:
            raise IngestionError(f"row {row_number}: language minScore is required")
        normalized.append({"languageTestType": test_type, "minScore": min_score})
    return sorted(normalized, key=lambda lr: (lr["languageTestType"], lr["minScore"]))


def read_csv(path: Path) -> list[dict[str, Any]]:
    with path.open(newline="", encoding="utf-8-sig") as handle:
        return list(csv.DictReader(handle))


def xlsx_cell_value(cell: ElementTree.Element, shared_strings: list[str], ns: dict[str, str]) -> str:
    cell_type = cell.attrib.get("t")
    value_node = cell.find("x:v", ns)
    if value_node is None:
        inline = cell.find("x:is/x:t", ns)
        return inline.text if inline is not None and inline.text is not None else ""
    value = value_node.text or ""
    if cell_type == "s":
        return shared_strings[int(value)]
    return value


def column_index(cell_ref: str) -> int:
    letters = re.sub(r"[^A-Z]", "", cell_ref.upper())
    index = 0
    for char in letters:
        index = index * 26 + ord(char) - ord("A") + 1
    return index - 1


def read_xlsx(path: Path) -> list[dict[str, Any]]:
    ns = {"x": "http://schemas.openxmlformats.org/spreadsheetml/2006/main"}
    with zipfile.ZipFile(path) as archive:
        shared_strings: list[str] = []
        if "xl/sharedStrings.xml" in archive.namelist():
            root = ElementTree.fromstring(archive.read("xl/sharedStrings.xml"))
            for item in root.findall("x:si", ns):
                shared_strings.append("".join(t.text or "" for t in item.findall(".//x:t", ns)))
        sheet_name = "xl/worksheets/sheet1.xml"
        root = ElementTree.fromstring(archive.read(sheet_name))
    rows: list[list[str]] = []
    for row in root.findall(".//x:sheetData/x:row", ns):
        values: list[str] = []
        for cell in row.findall("x:c", ns):
            idx = column_index(cell.attrib.get("r", "A1"))
            while len(values) <= idx:
                values.append("")
            values[idx] = xlsx_cell_value(cell, shared_strings, ns)
        rows.append(values)
    if not rows:
        return []
    headers = [clean(h) for h in rows[0]]
    result = []
    for row in rows[1:]:
        if not any(clean(v) for v in row):
            continue
        result.append({headers[i]: row[i] if i < len(row) else "" for i in range(len(headers))})
    return result


def load_rows(path: Path) -> list[dict[str, Any]]:
    if not path.exists():
        raise IngestionError(f"input file not found: {path}")
    suffix = path.suffix.lower()
    if suffix == ".csv":
        return read_csv(path)
    if suffix == ".xlsx":
        return read_xlsx(path)
    raise IngestionError("supported input formats are .csv and .xlsx")


def parse_rows(raw_rows: list[dict[str, Any]]) -> list[ParsedRow]:
    parsed = []
    for idx, raw in enumerate(raw_rows, start=2):
        row = {clean(k): v for k, v in raw.items() if clean(k)}
        term_name = require(row.get("term_name"), "term_name", idx)
        if not TERM_RE.match(term_name):
            raise IngestionError(f"row {idx}: term_name must match YYYY-N")
        semester = empty_to_none(row.get("semester_available_for_dispatch"))
        if semester and semester not in ALLOWED_SEMESTERS:
            raise IngestionError(f"row {idx}: unsupported semester_available_for_dispatch {semester}")
        parsed.append(ParsedRow(
            row_number=idx,
            term_name=term_name,
            home_university_name=require(row.get("home_university_name"), "home_university_name", idx),
            home_max_choice_count=parse_int(row.get("home_max_choice_count"), "home_max_choice_count", idx),
            home_email_domain=empty_to_none(row.get("home_email_domain")),
            host_korean_name=require(row.get("host_korean_name"), "host_korean_name", idx),
            host_english_name=empty_to_none(row.get("host_english_name")),
            host_format_name=empty_to_none(row.get("host_format_name")),
            country_code=empty_to_none(row.get("country_code")),
            region_code=empty_to_none(row.get("region_code")),
            univ_apply_info_id=parse_int(row.get("univ_apply_info_id"), "univ_apply_info_id", idx),
            student_capacity=parse_int(row.get("student_capacity"), "student_capacity", idx),
            semester_available_for_dispatch=semester,
            semester_requirement=empty_to_none(row.get("semester_requirement")),
            details_for_language=empty_to_none(row.get("details_for_language")),
            gpa_requirement=empty_to_none(row.get("gpa_requirement")),
            gpa_requirement_criteria=empty_to_none(row.get("gpa_requirement_criteria")),
            details_for_accommodation=empty_to_none(row.get("details_for_accommodation")),
            extra_info=parse_extra_info(row.get("extra_info"), idx),
            language_requirements=parse_language_requirements(row.get("language_requirements"), idx),
            homepage_url=empty_to_none(row.get("homepage_url")),
            english_course_url=empty_to_none(row.get("english_course_url")),
            accommodation_url=empty_to_none(row.get("accommodation_url")),
            details_for_local=empty_to_none(row.get("details_for_local")),
            logo_file=empty_to_none(row.get("logo_file")),
            background_file=empty_to_none(row.get("background_file")),
        ))
    if not parsed:
        raise IngestionError("input contains no data rows")
    return parsed


class ApiClient:
    def __init__(self, base_url: str, access_token: str | None) -> None:
        self.base_url = base_url.rstrip("/")
        self.access_token = access_token
        self.context = ssl.create_default_context()

    def request_json(self, method: str, path: str, body: Any | None = None, query: dict[str, Any] | None = None) -> Any:
        url = self.base_url + path
        if query:
            url += "?" + urlencode({k: v for k, v in query.items() if v is not None})
        data = None
        headers = {"Accept": "application/json"}
        if body is not None:
            data = json.dumps(body, ensure_ascii=False).encode("utf-8")
            headers["Content-Type"] = "application/json"
        if self.access_token:
            headers["Authorization"] = f"Bearer {self.access_token}"
        request = Request(url, data=data, headers=headers, method=method)
        try:
            with urlopen(request, context=self.context, timeout=30) as response:
                payload = response.read().decode("utf-8")
                return json.loads(payload) if payload else None
        except HTTPError as exc:
            detail = exc.read().decode("utf-8", errors="replace")
            raise IngestionError(f"{method} {path} failed with HTTP {exc.code}: {detail}") from exc
        except URLError as exc:
            raise IngestionError(f"{method} {path} failed: {exc.reason}") from exc

    def request_multipart(self, path: str, request_part: dict[str, Any], files: dict[str, Path]) -> Any:
        boundary = "----solidconnection" + uuid.uuid4().hex
        body = bytearray()

        def add_part(name: str, content: bytes, filename: str | None, content_type: str) -> None:
            body.extend(f"--{boundary}\r\n".encode())
            disposition = f'Content-Disposition: form-data; name="{name}"'
            if filename:
                disposition += f'; filename="{filename}"'
            body.extend((disposition + "\r\n").encode())
            body.extend(f"Content-Type: {content_type}\r\n\r\n".encode())
            body.extend(content)
            body.extend(b"\r\n")

        add_part("request", json.dumps(request_part, ensure_ascii=False).encode("utf-8"), None, "application/json")
        for field_name, path_value in files.items():
            content_type = mimetypes.guess_type(path_value.name)[0] or "application/octet-stream"
            add_part(field_name, path_value.read_bytes(), path_value.name, content_type)
        body.extend(f"--{boundary}--\r\n".encode())
        headers = {
            "Accept": "application/json",
            "Content-Type": f"multipart/form-data; boundary={boundary}",
        }
        if self.access_token:
            headers["Authorization"] = f"Bearer {self.access_token}"
        request = Request(self.base_url + path, data=bytes(body), headers=headers, method="POST")
        try:
            with urlopen(request, context=self.context, timeout=60) as response:
                return json.loads(response.read().decode("utf-8"))
        except HTTPError as exc:
            detail = exc.read().decode("utf-8", errors="replace")
            raise IngestionError(f"POST {path} failed with HTTP {exc.code}: {detail}") from exc

    def sign_in(self, email: str, password: str) -> None:
        response = self.request_json("POST", "/admin/auth/sign-in", {"email": email, "password": password})
        token = response.get("accessToken") if isinstance(response, dict) else None
        if not token:
            raise IngestionError("admin sign-in response did not contain accessToken")
        self.access_token = token


def enforce_dev_url(base_url: str) -> str:
    normalized = base_url.rstrip("/")
    if normalized != APPROVED_DEV_BASE_URL:
        raise IngestionError(f"refusing non-dev target: {base_url}")
    return normalized


def resolve_asset(path_text: str | None, assets_dir: Path | None) -> Path | None:
    if not path_text:
        return None
    path = Path(path_text)
    if not path.is_absolute() and assets_dir:
        path = assets_dir / path
    return path if path.exists() and path.is_file() else None


def fetch_all_terms(api: ApiClient) -> dict[str, dict[str, Any]]:
    return {item["name"]: item for item in api.request_json("GET", "/admin/terms")}


def fetch_all_home_universities(api: ApiClient) -> dict[str, dict[str, Any]]:
    return {item["name"]: item for item in api.request_json("GET", "/admin/home-universities")}


def fetch_all_host_universities(api: ApiClient) -> dict[str, dict[str, Any]]:
    by_name: dict[str, dict[str, Any]] = {}
    page = 0
    while True:
        response = api.request_json("GET", "/admin/host-universities", query={"page": page, "size": 100})
        for item in response.get("content", []):
            for name_key in ("koreanName", "englishName", "formatName"):
                name = item.get(name_key)
                if name:
                    by_name[name] = item
        total_pages = int(response.get("totalPages", 0))
        page += 1
        if page >= total_pages:
            break
    return by_name


def validate_missing_entity_fields(rows: list[ParsedRow], homes: dict[str, Any], hosts: dict[str, Any]) -> None:
    for row in rows:
        if row.home_university_name not in homes and row.home_max_choice_count is None:
            raise IngestionError(f"row {row.row_number}: home_max_choice_count is required for a missing home university")
        if row.host_korean_name not in hosts:
            for field_name, value in (
                ("host_english_name", row.host_english_name),
                ("host_format_name", row.host_format_name),
                ("country_code", row.country_code),
                ("region_code", row.region_code),
            ):
                if not value:
                    raise IngestionError(f"row {row.row_number}: {field_name} is required for a missing host university")


def build_plan(rows: list[ParsedRow], api: ApiClient, assets_dir: Path | None) -> tuple[dict[str, Any], dict[str, Any]]:
    terms = fetch_all_terms(api)
    homes = fetch_all_home_universities(api)
    hosts = fetch_all_host_universities(api)
    validate_missing_entity_fields(rows, homes, hosts)
    missing_assets = []
    lookup_failures = []
    will_create_apply_infos = 0
    will_update_apply_infos = 0
    for row in rows:
        if row.host_korean_name in hosts:
            term = terms.get(row.term_name)
            home = homes.get(row.home_university_name)
            host = hosts.get(row.host_korean_name)
            if term and home and host:
                try:
                    existing = find_existing_apply_info(api, row, term["id"], home["id"], host["id"])
                    if existing:
                        will_update_apply_infos += 1
                    else:
                        will_create_apply_infos += 1
                except IngestionError as exc:
                    lookup_failures.append({"row": row.row_number, "error": str(exc)})
            else:
                will_create_apply_infos += 1
        else:
            logo_path = resolve_asset(row.logo_file, assets_dir)
            background_path = resolve_asset(row.background_file, assets_dir)
            if not logo_path or not background_path:
                missing_assets.append({
                    "row": row.row_number,
                    "host_korean_name": row.host_korean_name,
                    "host_english_name": row.host_english_name,
                    "required": {
                        "logo_file": row.logo_file or f"{slug(row.host_english_name or row.host_korean_name)}-logo",
                        "background_file": row.background_file or f"{slug(row.host_english_name or row.host_korean_name)}-background",
                    },
                })
            will_create_apply_infos += 1
    status = "failed" if lookup_failures else "needs-assets" if missing_assets else "preflight-ok"
    plan = {
        "status": status,
        "rows": len(rows),
        "missing_assets": missing_assets,
        "lookup_failures": lookup_failures,
        "will_create_terms": sorted({r.term_name for r in rows if r.term_name not in terms}),
        "will_create_home_universities": sorted({r.home_university_name for r in rows if r.home_university_name not in homes}),
        "will_create_host_universities": sorted({r.host_korean_name for r in rows if r.host_korean_name not in hosts}),
        "will_create_univ_apply_infos": will_create_apply_infos,
        "will_update_univ_apply_infos": will_update_apply_infos,
    }
    indexes = {"terms": terms, "homes": homes, "hosts": hosts}
    return plan, indexes


def slug(value: str) -> str:
    return re.sub(r"[^a-z0-9]+", "-", value.lower()).strip("-")


def find_existing_apply_info(
    api: ApiClient,
    row: ParsedRow,
    term_id: int,
    home_id: int,
    host_id: int,
) -> dict[str, Any] | None:
    matches = api.request_json("GET", "/admin/univ-apply-infos", query={
        "termId": term_id,
        "homeUniversityId": home_id,
        "hostUniversityId": host_id,
    })
    if not isinstance(matches, list):
        raise IngestionError("natural-key lookup did not return a list")
    if len(matches) > 1:
        raise IngestionError(
            f"duplicate UnivApplyInfo rows for termId={term_id}, homeUniversityId={home_id}, hostUniversityId={host_id}"
        )
    if row.univ_apply_info_id is not None:
        if matches and int(matches[0]["id"]) != row.univ_apply_info_id:
            raise IngestionError(
                f"univ_apply_info_id {row.univ_apply_info_id} does not match natural-key row {matches[0]['id']}"
            )
        if not matches:
            fetched = api.request_json("GET", f"/admin/univ-apply-infos/{row.univ_apply_info_id}")
            if (
                int(fetched.get("termId")) != int(term_id)
                or int(fetched.get("homeUniversityId")) != int(home_id)
                or int(fetched.get("hostUniversityId")) != int(host_id)
            ):
                raise IngestionError(
                    f"univ_apply_info_id {row.univ_apply_info_id} does not match the row's term/home/host natural key"
                )
            return fetched
    return matches[0] if matches else None


def apply_rows(rows: list[ParsedRow], api: ApiClient, assets_dir: Path | None, indexes: dict[str, Any]) -> dict[str, Any]:
    counts = {
        "terms_created": 0,
        "terms_reused": 0,
        "home_universities_created": 0,
        "home_universities_reused": 0,
        "host_universities_created": 0,
        "host_universities_reused": 0,
        "univ_apply_infos_created": 0,
        "univ_apply_infos_updated": 0,
        "failed": 0,
    }
    row_results = []
    terms = indexes["terms"]
    homes = indexes["homes"]
    hosts = indexes["hosts"]

    for term_name in sorted({r.term_name for r in rows}):
        if term_name in terms:
            counts["terms_reused"] += 1
        else:
            created = api.request_json("POST", "/admin/terms", {"name": term_name})
            terms[term_name] = created
            counts["terms_created"] += 1

    for row in rows:
        try:
            home = homes.get(row.home_university_name)
            if home:
                counts["home_universities_reused"] += 1
            else:
                home = api.request_json("POST", "/admin/home-universities", {
                    "name": row.home_university_name,
                    "maxChoiceCount": row.home_max_choice_count,
                    "emailDomain": row.home_email_domain,
                })
                homes[row.home_university_name] = home
                counts["home_universities_created"] += 1

            host = hosts.get(row.host_korean_name)
            if host:
                counts["host_universities_reused"] += 1
            else:
                logo_path = resolve_asset(row.logo_file, assets_dir)
                background_path = resolve_asset(row.background_file, assets_dir)
                if not logo_path or not background_path:
                    raise IngestionError("missing host university image files after preflight")
                host = api.request_multipart("/admin/host-universities", {
                    "koreanName": row.host_korean_name,
                    "englishName": row.host_english_name,
                    "formatName": row.host_format_name,
                    "homepageUrl": row.homepage_url,
                    "englishCourseUrl": row.english_course_url,
                    "accommodationUrl": row.accommodation_url,
                    "detailsForLocal": row.details_for_local,
                    "countryCode": row.country_code,
                    "regionCode": row.region_code,
                }, {"logoFile": logo_path, "backgroundFile": background_path})
                hosts[row.host_korean_name] = host
                counts["host_universities_created"] += 1

            payload = apply_payload(row)
            existing_apply_info = find_existing_apply_info(
                api, row, terms[row.term_name]["id"], home["id"], host["id"]
            )
            if existing_apply_info is None:
                response = api.request_json("POST", "/admin/univ-apply-infos", {
                    "termId": terms[row.term_name]["id"],
                    "homeUniversityId": home["id"],
                    "hostUniversityId": host["id"],
                    **payload,
                })
                counts["univ_apply_infos_created"] += 1
            else:
                response = api.request_json("PATCH", f"/admin/univ-apply-infos/{existing_apply_info['id']}", payload)
                counts["univ_apply_infos_updated"] += 1

            verification = verify_row(api, row, response["id"], terms[row.term_name]["id"], home["id"], host["id"])
            row_results.append({"row": row.row_number, "id": response["id"], "status": "verified", "verification": verification})
        except IngestionError as exc:
            counts["failed"] += 1
            row_results.append({"row": row.row_number, "status": "failed", "error": str(exc)})

    status = "verified" if counts["failed"] == 0 else "failed"
    return {"status": status, "counts": counts, "rows": row_results}


def apply_payload(row: ParsedRow) -> dict[str, Any]:
    return {
        "studentCapacity": row.student_capacity,
        "semesterAvailableForDispatch": row.semester_available_for_dispatch,
        "semesterRequirement": row.semester_requirement,
        "detailsForLanguage": row.details_for_language,
        "gpaRequirement": row.gpa_requirement,
        "gpaRequirementCriteria": row.gpa_requirement_criteria,
        "detailsForAccommodation": row.details_for_accommodation,
        "extraInfo": row.extra_info,
        "languageRequirements": row.language_requirements,
    }


def verify_row(api: ApiClient, row: ParsedRow, apply_info_id: int, term_id: int, home_id: int, host_id: int) -> dict[str, Any]:
    fetched = api.request_json("GET", f"/admin/univ-apply-infos/{apply_info_id}")
    expected = {
        "termId": term_id,
        "homeUniversityId": home_id,
        "hostUniversityId": host_id,
        "koreanName": row.host_korean_name,
        **apply_payload(row),
    }
    mismatches = []
    for key, expected_value in expected.items():
        actual = fetched.get(key)
        if key == "languageRequirements":
            actual = sorted(actual or [], key=lambda lr: (lr.get("languageTestType"), lr.get("minScore")))
        if actual != expected_value:
            mismatches.append({"field": key, "expected": expected_value, "actual": actual})
    if mismatches:
        raise IngestionError(f"verification mismatch for univ_apply_info {apply_info_id}: {json.dumps(mismatches, ensure_ascii=False)}")
    return {"checked_fields": sorted(expected.keys())}


def output(payload: dict[str, Any]) -> None:
    print(json.dumps(payload, ensure_ascii=False, indent=2, sort_keys=True))


def prompt_for_admin_credentials() -> tuple[str, str]:
    if not sys.stdin.isatty():
        raise IngestionError(
            "provide --access-token or both --admin-email and --admin-password when stdin is not interactive"
        )
    email = input("Stage admin email: ").strip()
    password = getpass.getpass("Stage admin password: ")
    if not email or not password:
        raise IngestionError("stage admin email and password are required")
    return email, password


def self_test() -> None:
    sample = [{
        "term_name": "2026-1",
        "home_university_name": "Inha",
        "home_max_choice_count": "3",
        "host_korean_name": "Sample",
        "host_english_name": "Sample University",
        "host_format_name": "Sample",
        "country_code": "US",
        "region_code": "US-CA",
        "language_requirements": "TOEFL_IBT:80;IELTS:6.5",
        "extra_info": "note=ok",
    }]
    parsed = parse_rows(sample)
    assert parsed[0].language_requirements == [
        {"languageTestType": "IELTS", "minScore": "6.5"},
        {"languageTestType": "TOEFL_IBT", "minScore": "80"},
    ]
    assert parsed[0].extra_info == {"note": "ok"}

    class FakeApi:
        def request_json(self, method: str, path: str) -> dict[str, Any]:
            assert method == "GET"
            assert path == "/admin/univ-apply-infos/99"
            return {
                "termId": 1,
                "homeUniversityId": 2,
                "hostUniversityId": 3,
                "koreanName": "Wrong Korean Name",
                "studentCapacity": None,
                "semesterAvailableForDispatch": None,
                "semesterRequirement": None,
                "detailsForLanguage": None,
                "gpaRequirement": None,
                "gpaRequirementCriteria": None,
                "detailsForAccommodation": None,
                "extraInfo": {"note": "ok"},
                "languageRequirements": parsed[0].language_requirements,
            }

    try:
        verify_row(FakeApi(), parsed[0], 99, 1, 2, 3)
    except IngestionError as exc:
        assert "koreanName" in str(exc)
    else:
        raise AssertionError("verify_row must fail when fetched koreanName does not match the input host_korean_name")
    output({"status": "self-test-ok"})


def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Ingest university data into Solid Connection dev.")
    parser.add_argument("--mode", choices=["preflight", "apply"], required=False)
    parser.add_argument("--input", type=Path)
    parser.add_argument("--assets-dir", type=Path)
    parser.add_argument("--dev-base-url", default=os.environ.get("SOLID_CONNECT_DEV_API_BASE", APPROVED_DEV_BASE_URL))
    parser.add_argument("--admin-email", default=os.environ.get("SOLID_CONNECT_ADMIN_EMAIL"))
    parser.add_argument("--admin-password", default=os.environ.get("SOLID_CONNECT_ADMIN_PASSWORD"))
    parser.add_argument("--access-token", default=os.environ.get("SOLID_CONNECT_ADMIN_ACCESS_TOKEN"))
    parser.add_argument("--manifest-output", type=Path)
    parser.add_argument("--self-test", action="store_true")
    return parser.parse_args(argv)


def main(argv: list[str]) -> int:
    args = parse_args(argv)
    try:
        if args.self_test:
            self_test()
            return 0
        if not args.mode or not args.input:
            raise IngestionError("--mode and --input are required unless --self-test is used")
        base_url = enforce_dev_url(args.dev_base_url)
        raw_rows = load_rows(args.input)
        rows = parse_rows(raw_rows)
        api = ApiClient(base_url, args.access_token)
        if not api.access_token:
            if not args.admin_email or not args.admin_password:
                args.admin_email, args.admin_password = prompt_for_admin_credentials()
            api.sign_in(args.admin_email, args.admin_password)
        plan, indexes = build_plan(rows, api, args.assets_dir)
        if args.manifest_output:
            args.manifest_output.write_text(json.dumps(plan, ensure_ascii=False, indent=2), encoding="utf-8")
        if args.mode == "preflight" or plan["status"] in {"needs-assets", "failed"}:
            output(plan)
            return 0 if plan["status"] in {"preflight-ok", "needs-assets"} else 1
        result = apply_rows(rows, api, args.assets_dir, indexes)
        output(result)
        return 0 if result["status"] == "verified" else 1
    except (IngestionError, json.JSONDecodeError, KeyError, zipfile.BadZipFile) as exc:
        output({"status": "failed", "error": str(exc)})
        return 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
