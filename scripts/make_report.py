#!/usr/bin/env python3
"""Assemble a build report from CI step logs.

The report is published as a GitHub job summary and run artifact. It is no
longer committed back to the repository by the workflow.
"""
import glob
import json
import os
import re
import sys
import xml.etree.ElementTree as ET
from datetime import datetime, timezone

steps = [
    ("engine_selfcheck", "/tmp/engine.log"),
    ("engine_unit_tests", "/tmp/engine_test.log"),
    ("app_unit_tests", "/tmp/app_test.log"),
    ("assemble_debug", "/tmp/assemble.log"),
    ("assemble_release", "/tmp/release.log"),
    ("lint", "/tmp/lint.log"),
    ("quality", "/tmp/quality.log"),
    ("android_test_compile", "/tmp/android_test_compile.log"),
    ("kover", "/tmp/kover.log"),
]

out = []
out.append("# Build report\n")
out.append(f"Generated: {datetime.now(timezone.utc).isoformat()}\n")
out.append(f"Commit: `{os.environ.get('GITHUB_SHA', 'local')}`\n")

status = {}
for name, path in steps:
    if os.path.exists(path + ".code"):
        try:
            code = int(open(path + ".code").read().strip())
        except ValueError:
            code = -1
    else:
        code = -1 if not os.path.exists(path) else 0
    status[name] = code

out.append("## Step status\n")
out.append("| Step | Exit code | Result |")
out.append("|---|---|---|")
for name, _ in steps:
    code = status[name]
    result = "PASS" if code == 0 else ("SKIPPED" if code == 127 else "FAIL")
    out.append(f"| {name} | {code} | {result} |")
out.append("")

if os.path.exists("/tmp/step-outcomes.txt"):
    out.append("## Step outcomes\n")
    out.append("| Step | Outcome |")
    out.append("|---|---|")
    for line in open("/tmp/step-outcomes.txt"):
        if "=" in line:
            key, value = line.strip().split("=", 1)
            out.append(f"| {key} | {value or 'not run'} |")
    out.append("")

# First compiler/test errors, so a red build explains itself from the report.
errors = []
for log in [
    "/tmp/assemble.log",
    "/tmp/release.log",
    "/tmp/lint.log",
    "/tmp/quality.log",
    "/tmp/android_test_compile.log",
    "/tmp/kover.log",
]:
    if not os.path.exists(log):
        continue
    text = open(log, errors="replace").read()
    for line in text.splitlines():
        if re.match(r"^e: |^.*error: ", line):
            errors.append(line.strip()[:300])
        if len(errors) >= 25:
            break
if errors:
    out.append("## Build errors\n")
    out.append("```")
    out.extend(errors)
    out.append("```")
    out.append("")

# Engine self-check table (source of truth lives in the JVM run).
report = "/tmp/engine-report.md"
if os.path.exists(report):
    out.append("## Engine self-check\n")
    out.append(open(report).read())
else:
    out.append("## Engine self-check\n_no report produced_\n")


def summarize(pattern, title):
    files = glob.glob(pattern, recursive=True)
    if not files:
        return
    out.append(f"<!-- {len(files)} result files -->")
    tests = failures = errors = skipped = 0
    failed_names = []
    for f in files:
        try:
            txt = open(f).read()
        except OSError:
            continue
        for suite in re.finditer(r"<testsuite\b([^>]*)>", txt):
            attrs = dict(re.findall(r'(\w+)="([^"]*)"', suite.group(1)))

            def num(key):
                try:
                    return int(attrs.get(key, "0"))
                except ValueError:
                    return 0

            tests += num("tests")
            failures += num("failures")
            errors += num("errors")
            skipped += num("skipped")
        for m in re.finditer(
            r'<testcase name="([^"]+)" classname="([^"]+)"[^>]*>\s*<(?:failure|error)[^>]*message="([^"]*)"',
            txt,
        ):
            msg = (m.group(3) or "").replace("&quot;", '"').replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&")
            failed_names.append(f"{m.group(2)}.{m.group(1)} — {msg[:300]}")
    out.append(f"## {title}\n")
    out.append(f"{tests} tests, {failures} failures, {errors} errors, {skipped} skipped\n")
    if failed_names:
        out.append("Failing: " + ", ".join(failed_names[:20]) + "\n")


summarize("engine/build/test-results/**/*.xml", "Engine unit tests")
summarize("app/build/test-results/**/*.xml", "App unit tests")


def kover_line_coverage():
    """Return (human-readable coverage, report path) from the first Kover XML."""
    for path in sorted(glob.glob("engine/build/reports/kover/**/*.xml", recursive=True)):
        try:
            root = ET.parse(path).getroot()
        except ET.ParseError:
            continue
        for counter in root.iter("counter"):
            if counter.get("type") != "LINE":
                continue
            missed = int(counter.get("missed", "0"))
            covered = int(counter.get("covered", "0"))
            total = missed + covered
            if total > 0:
                return f"{covered}/{total} lines ({covered / total * 100:.1f}%)", path
    return None, None


coverage, coverage_path = kover_line_coverage()
os.makedirs("docs/baseline", exist_ok=True)
if coverage:
    out.append("## Kover engine line coverage\n")
    out.append(f"{coverage} — `{coverage_path}`\n")
    open("docs/baseline/kover-coverage.txt", "w").write(f"{coverage}\n")
else:
    out.append("## Kover engine line coverage\n_no XML report produced_\n")
    open("docs/baseline/kover-coverage.txt", "w").write("_no XML report produced_\n")

out.append("## Artifacts\n")
for pattern in [
    "app/build/outputs/apk/debug/*.apk",
    "app/build/outputs/apk/release/*.apk",
    "app/build/outputs/bundle/release/*.aab",
]:
    for f in glob.glob(pattern):
        size = os.path.getsize(f)
        out.append(f"- `{f}` — {size / 1048576:.1f} MiB")
out.append("")


def extract_errors(path, title):
    if not os.path.exists(path):
        return
    txt = open(path, errors="replace").read()
    lines = [line for line in txt.splitlines() if line.startswith("e: ") or line.startswith("w: ")]
    errors = [line for line in lines if line.startswith("e: ")]
    if not errors:
        return
    files = {}
    for line in errors:
        m = re.search(r"/([^/]+)\.kt:(\d+):(\d+)", line)
        if m:
            files.setdefault(m.group(1), []).append(line.split(" ", 1)[-1])
    out.append(f"## {title}: compiler errors ({len(errors)})\n")
    out.append("| File | Errors |")
    out.append("|---|---|")
    for name, errs in sorted(files.items(), key=lambda kv: -len(kv[1])):
        out.append(f"| `{name}` | {len(errs)} |")
    out.append("")
    out.append("```")
    out.extend(sorted(set(errors))[:300])
    out.append("```\n")


for path, title in [
    ("/tmp/assemble.log", "assembleDebug"),
    ("/tmp/release.log", "release"),
    ("/tmp/app_test.log", "app tests"),
    ("/tmp/engine_test.log", "engine tests"),
    ("/tmp/quality.log", "quality"),
    ("/tmp/lint.log", "lint"),
    ("/tmp/android_test_compile.log", "androidTest compile"),
]:
    extract_errors(path, title)

for path, title in [
    ("/tmp/engine.log", "engine self-check"),
    ("/tmp/engine_test.log", "engine tests"),
    ("/tmp/app_test.log", "app tests"),
    ("/tmp/assemble.log", "assembleDebug"),
    ("/tmp/release.log", "release"),
    ("/tmp/quality.log", "quality"),
    ("/tmp/lint.log", "lint"),
    ("/tmp/android_test_compile.log", "androidTest compile"),
]:
    if not os.path.exists(path):
        continue
    lines = open(path, errors="replace").read().splitlines()
    for i, line in enumerate(lines):
        if "What went wrong" in line or line.startswith("e: file") or line.startswith("e: "):
            block = [item for item in lines[i : i + 40] if item.strip()]
            out.append(f"## {title}: failure detail\n")
            out.append("```")
            out.extend(block[:40])
            out.append("```\n")
            break

for path, title in [
    ("/tmp/assemble.log", "assembleDebug"),
    ("/tmp/release.log", "release"),
    ("/tmp/quality.log", "quality"),
    ("/tmp/lint.log", "lint"),
    ("/tmp/android_test_compile.log", "androidTest compile"),
]:
    if os.path.exists(path):
        lines = [line for line in open(path, errors="replace").read().splitlines() if line.strip()]
        out.append(f"## {title}: log tail\n")
        out.append("```")
        out.extend(lines[-25:])
        out.append("```\n")

open("BUILD_REPORT.md", "w").write("\n".join(out))
print("\n".join(out))
sys.exit(0)
