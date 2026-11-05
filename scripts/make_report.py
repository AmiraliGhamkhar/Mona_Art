#!/usr/bin/env python3
"""Assembles a build report from CI step logs so results are inspectable from
the repository itself (CI artifact storage is not always reachable)."""
import glob
import json
import os
import re
import sys
from datetime import datetime, timezone

steps = [
    ("engine_selfcheck", "/tmp/engine.log"),
    ("engine_unit_tests", "/tmp/engine_test.log"),
    ("app_unit_tests", "/tmp/app_test.log"),
    ("assemble_debug", "/tmp/assemble.log"),
    ("assemble_release", "/tmp/release.log"),
    ("lint", "/tmp/lint.log"),
]

out = []
out.append("# Build report\n")
out.append(f"Generated: {datetime.now(timezone.utc).isoformat()}\n")
out.append(f"Commit: `{os.environ.get('GITHUB_SHA', 'local')}`\n")

status = {}
for name, path in steps:
    code = 0
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

# Step outcomes as reported by the workflow, present even when a later step was
# skipped after an earlier failure.
outcomes = {}
if os.path.exists("/tmp/step-outcomes.txt"):
    for line in open("/tmp/step-outcomes.txt"):
        if "=" in line:
            k, v = line.strip().split("=", 1)
            outcomes[k] = v
if outcomes:
    out.append("## Step outcomes\n")
    out.append("| Step | Outcome |")
    out.append("|---|---|")
    for k, v in outcomes.items():
        out.append(f"| {k} | {v or 'not run'} |")
    out.append("")

# First compiler/test errors, so a red build explains itself from the repo.
errors = []
for log in ["/tmp/assemble.log", "/tmp/release.log"]:
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

# Release publishing log: the step never fails the build, so its output has to be
# visible here or a broken upload goes unnoticed.
if os.path.exists("/tmp/apk-release.log"):
    tail = open("/tmp/apk-release.log", errors="replace").read().strip().splitlines()[-25:]
    if tail:
        out.append("## APK release publishing\n")
        out.append("```")
        out.extend(tail)
        out.append("```")
        out.append("")

# Engine self-check table (source of truth lives in the JVM run)
report = "/tmp/engine-report.md"
if os.path.exists(report):
    text = open(report).read()
    out.append("## Engine self-check\n")
    out.append(text)
else:
    out.append("## Engine self-check\n_no report produced_\n")

# Unit test results from JUnit XML
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
        # Gradle writes the attributes in its own order, so read them per suite
        # instead of relying on positional regex groups.
        for suite in re.finditer(r'<testsuite\b([^>]*)>', txt):
            attrs = dict(re.findall(r'(\w+)="([^"]*)"', suite.group(1)))
            def num(key):
                try:
                    return int(attrs.get(key, "0"))
                except ValueError:
                    return 0
            tests += num("tests"); failures += num("failures")
            errors += num("errors"); skipped += num("skipped")
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

# Artifacts
out.append("## Artifacts\n")
for pattern in ["app/build/outputs/apk/debug/*.apk", "app/build/outputs/apk/release/*.apk",
                "app/build/outputs/bundle/release/*.aab"]:
    for f in glob.glob(pattern):
        size = os.path.getsize(f)
        out.append(f"- `{f}` — {size/1048576:.1f} MiB")
out.append("")

# Compiler errors and warnings worth reading
def extract_errors(path, title):
    """All compiler diagnostics, deduplicated, plus a per-file tally."""
    if not os.path.exists(path):
        return
    txt = open(path, errors="replace").read()
    lines = [l for l in txt.splitlines() if l.startswith("e: ") or l.startswith("w: ")]
    errors = [l for l in lines if l.startswith("e: ")]
    if not errors:
        return
    files = {}
    for l in errors:
        m = re.search(r'/([^/]+)\.kt:(\d+):(\d+)', l)
        if m:
            files.setdefault(m.group(1), []).append(l.split(" ", 1)[-1])
    out.append(f"## {title}: compiler errors ({len(errors)})\n")
    out.append("| File | Errors |")
    out.append("|---|---|")
    for f, errs in sorted(files.items(), key=lambda kv: -len(kv[1])):
        out.append(f"| `{f}` | {len(errs)} |")
    out.append("")
    out.append("```")
    out.extend(sorted(set(errors))[:300])
    out.append("```\n")

extract_errors("/tmp/assemble.log", "assembleDebug")
extract_errors("/tmp/release.log", "release")
extract_errors("/tmp/app_test.log", "app tests")
extract_errors("/tmp/engine_test.log", "engine tests")

# What-went-wrong blocks: the most actionable section of any Gradle failure
for path, title in [("/tmp/engine.log", "engine self-check"), ("/tmp/engine_test.log", "engine tests"),
                    ("/tmp/app_test.log", "app tests"), ("/tmp/assemble.log", "assembleDebug"),
                    ("/tmp/release.log", "release")]:
    if not os.path.exists(path):
        continue
    lines = open(path, errors="replace").read().splitlines()
    for i, line in enumerate(lines):
        if "What went wrong" in line or line.startswith("e: file") or line.startswith("e: "):
            block = [l for l in lines[i:i + 40] if l.strip()]
            out.append(f"## {title}: failure detail\n")
            out.append("```")
            out.extend(block[:40])
            out.append("```\n")
            break

# Tail of key logs to catch configuration problems
for path, title in [("/tmp/assemble.log", "assembleDebug"), ("/tmp/release.log", "release")]:
    if os.path.exists(path):
        txt = open(path, errors="replace").read().splitlines()
        tail = [l for l in txt if l.strip()][-25:]
        out.append(f"## {title}: log tail\n")
        out.append("```")
        out.extend(tail)
        out.append("```\n")

open("BUILD_REPORT.md", "w").write("\n".join(out))
print("\n".join(out))
sys.exit(0)
