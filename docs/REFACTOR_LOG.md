# Refactor log

## Phase 0 — baseline, guardrails, truth (2026-11-05)

**Result: gate NOT passed** (see `docs/baseline/phase-0-gate.md`). Phase 1 not
started.

### What changed

- `build.gradle.kts`: central Detekt/ktlint application to both modules; Kover
  plugin alias available to `:engine`.
- `engine/build.gradle.kts`: applies Kover; keeps the existing `selfCheck` task.
- `app/build.gradle.kts`: `lint.abortOnError = true`; adds `androidTest` JUnit
  and `androidx.test:core` dependencies for the smoke test.
- `gradle/libs.versions.toml`: adds Detekt/ktlint/Kover versions + plugin
  aliases; removes unused Coil, graphics-shapes and `kotlin-android` entries.
- `.editorconfig`, `config/detekt/detekt.yml`, `config/detekt/baseline.xml`:
  static-analysis configuration and seeded (empty) baseline.
- `.github/workflows/android.yml`: setup-java 17, Gradle caching, no `set +e`,
  quality/lint/Kover/androidTest-compile in the exit gate, report becomes job
  summary + artifact instead of a self-commit, rolling release removed.
- `.github/workflows/publish-apk.yml`: single manual release mechanism, now with
  the same Java/Gradle setup.
- `scripts/make_report.py`: includes quality/lint/Kover/androidTest steps and
  extracts Kover line coverage into `docs/baseline/kover-coverage.txt`.
- `gradle/wrapper/gradle-wrapper.properties`: adds verified upstream SHA-256.
- `.gitignore` + `git rm --cached jdk4py-*.whl`: stops tracking the 33.8 MiB
  wheel; adds `*.whl`.
- `app/proguard-rules.pro`: removes blanket engine keep and the non-existent
  `com.monaart.studio.document.**` rule; keeps only enum synthetic methods.
- `app/src/androidTest/java/com/monaart/studio/EditorSmokeTest.kt`: new
  launch → stroke → save → reload → pixel assertion smoke test.
- `docs/baseline/**`: baseline logs, reproduction commands, gate checklist and
  explicit statements of what could not be verified.

### Commands / key output

```
./gradlew ...                          → exit=1 (distribution host blocked)
engine main compile                    → 93 class files, warnings only
SelfCheck                              → 27/27 checks passed
engine JUnit suites (local shim)       → OK (48 tests)
stroke+flush                           → 0.170 ms/sample, p99 1.55 ms
```

### New/updated tests

- `EditorSmokeTest` (instrumented, source only; compile unverified).
- No existing engine/app test source was modified. The 48 engine tests were run
  against a non-committed local JUnit API shim because Maven Central is blocked.

### Not verified

Everything requiring Gradle, the Android SDK, an emulator, Detekt/ktlint/Kover
plugin resolution, or a release build. See `docs/baseline/phase-0-gate.md`.
