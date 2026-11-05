# Phase 0 baseline

This directory records the **actual** Phase 0 baseline produced in the Arena
sandbox on 2026-11-05. It is intentionally explicit about what could and could
not be executed here.

## Environment

- Branch: `arena/102e93bd-mona-art`, based on `96ee27b`.
- JDK: `OpenJDK 25.0.2` extracted from the repository's committed
  `jdk4py-25.0.2.1-py3-none-manylinux_2_17_x86_64.whl` (untracked in Phase 0).
- Kotlin: `2.4.21`, obtained from the npm registry-hosted compiler bundle.
- Gradle distribution: **unavailable** — `services.gradle.org` is outside the
  sandbox's allowlist, so `./gradlew` cannot download Gradle 9.8.2.
- Android SDK: **unavailable** — no SDK, emulator, or Robolectric dependencies.

## What is here

| File | Meaning |
|---|---|
| `gradle-baseline.log` | Exact output of the requested Phase 0.1 Gradle command, failing at distribution download. |
| `engine-compile.log` | Direct `K2JVMCompiler` output for `:engine` main sources. |
| `engine-test-compile.log` | Direct compile output for the four engine JUnit suites plus a local shim. |
| `selfcheck.log` | `SelfCheckKt` output with `--report`. |
| `selfcheck-report.md` | Machine-readable 27-check report. |
| `engine-tests.log` | 48 engine tests run through a local JUnit-API shim. |
| `jvm-toolchain.md` | Exact direct-JVM reproduction commands. |
| `phase-0-gate.md` | The hard gate with ✓/✗ and reasons. |
| `release-size.md` | Pre-change release sizes from the last self-committed CI report; post-change size is **not** measured. |
| `kover-coverage.md` / `kover-coverage.txt` | Starting line coverage: **not measured** (Kover needs Gradle). |
| `app-baseline-unavailable.md` | Why `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:lintDebug` could not run. |
| `android-test-smoke.md` | Smoke-test source added; compile not verified. |
| `detekt-baseline.md` | Baseline seeded but not generated. |
| `gradle-wrapper-checksum.md` | Provenance of the wrapper SHA-256. |

**Important:** the engine test run used a local, non-committed JUnit 4 API shim
because Maven Central is unavailable. It is not a replacement for the real
`:engine:test` Gradle task; in a full environment use the Gradle command in the
gate report. The shim runner implements only the assertion/annotation surface
used by the existing 48 tests and does not change any test source.
