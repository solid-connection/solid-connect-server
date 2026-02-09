#!/usr/bin/env python3
import json
import sys
import re

data = json.load(sys.stdin)
file_path = data.get("tool_input", {}).get("file_path", "")

if not file_path.endswith(".java") or not file_path:
    sys.exit(0)

try:
    with open(file_path) as f:
        content = f.read()
        lines = content.split("\n")
except Exception:
    sys.exit(0)

warnings = []

# 1. 와일드카드 import 체크
for i, line in enumerate(lines, 1):
    if re.match(r"\s*import\s+.*\.\*;", line):
        warnings.append(f"L{i}: 와일드카드 import 발견 -> 명시적 import 필요")

# 2. 파일 끝 줄바꿈 체크
if content and not content.endswith("\n"):
    warnings.append("파일 끝 줄바꿈 누락")

# 3. Entity 클래스의 @Column 체크
if "@Entity" in content:
    field_pattern = re.compile(r"^\s+private\s+\w+(?:<[^>]+>)?\s+\w+;")
    relation_annotations = {
        "@Column", "@Id", "@ManyToOne", "@OneToMany",
        "@JoinColumn", "@OneToOne", "@ManyToMany",
        "@Transient", "@Version", "@Embedded", "@EmbeddedId",
    }
    for i, line in enumerate(lines):
        if field_pattern.match(line):
            preceding = "\n".join(lines[max(0, i - 5):i])
            has_annotation = any(ann in preceding for ann in relation_annotations)
            if not has_annotation:
                warnings.append(f"L{i + 1}: Entity 필드에 @Column 누락 가능성: {line.strip()}")

if warnings:
    print(f"[컨벤션 체크 - {file_path.split('/')[-1]}]")
    for w in warnings:
        print(f"  - {w}")
